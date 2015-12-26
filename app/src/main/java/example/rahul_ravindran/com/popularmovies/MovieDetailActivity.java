package example.rahul_ravindran.com.popularmovies;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import example.rahul_ravindran.com.popularmovies.adapters.GridViewImageAdapter;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import example.rahul_ravindran.com.popularmovies.ui.ResizableImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by rahulravindran on 16/12/15.
 */


public class MovieDetailActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {

    ObservableScrollView mScrollView;
    @Bind(R.id.movie_cover_container) FrameLayout mCoverContainer;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.movie_cover) ResizableImageView coverImageView;
    @Bind(R.id.movie_poster) ResizableImageView moviePosterImageView;
    @Bind(R.id.movie_title) TextView movieTitle;
    @Bind(R.id.movie_release_date) TextView movieReleaseDate;
    @Bind(R.id.movie_average_rating) TextView movieAverageRating;
    @Bind(R.id.movie_overview) TextView movieOverview;
    @Bind(R.id.ratingBars) RatingBar movieRatingBar;
    @Bind(R.id.movie_reviews_container) ViewGroup mReviewsGroup;
    @BindColor(R.color.theme_primary) int mColorThemePrimary;
    @BindColor(R.color.body_text_white) int mColorTextWhite;



    private String baseImageUrl = "http://image.tmdb.org/t/p/w342/";
    private CompositeSubscription mSubscriptions;
    private MoviesRepoImpl mMoviesRepository;
    private MovieDB mMovie;
    private List<MovieReview> mReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mSubscriptions = new CompositeSubscription();
        OkHttpClient client = new OkHttpClient();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        MoviesAPI apiService = retrofit.create(MoviesAPI.class);
        mMoviesRepository = new MoviesRepoImpl(apiService);


        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0.2f, mColorThemePrimary));
            mToolbar.setTitleTextColor(ScrollUtils.getColorWithAlpha(0.2f, mColorTextWhite));
            ViewCompat.setElevation(mToolbar, getResources().getDimension(R.dimen.toolbar_elevation));
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MovieDetailActivity.this.finish();
                }
            });

            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setDisplayShowHomeEnabled(true);
            }
        }
        mScrollView = (ObservableScrollView) findViewById(R.id.movie_scroll_view);

        mScrollView.setScrollViewCallbacks(this);


        mMovie = (MovieDB) getIntent().getExtras().get("EXTRA_MOVIE");

        Picasso.with(this).load(baseImageUrl +
                mMovie.getBackdropPath()).into(coverImageView);

        Picasso.with(this).load(baseImageUrl +
                mMovie.getBackdropPath()).fit().into(moviePosterImageView);

        movieTitle.setText(mMovie.getTitle());
        movieReleaseDate.setText("Release Date:"+mMovie.getReleaseDate());
        movieAverageRating.setText("Average Rating:"+Double.toString(mMovie.getVoteAverage()));
        movieOverview.setText(mMovie.getOverview());
        movieRatingBar.setNumStars(10);
        movieRatingBar.setRating((float) mMovie.getVoteAverage());

    }

    private void loadReviews() {
        mSubscriptions.add(mMoviesRepository.getMovieReview(mMovie.getId())
        .subscribe(new Action1<List<MovieReview>>() {
            @Override
            public void call(List<MovieReview> reviews) {
                Timber.d(String.format("Reviews loaded, %d items.", reviews.size()));
                onReviewsLoaded(reviews);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Timber.e(throwable, "Reviews loading failed.");
                onReviewsLoaded(null);
            }
        }));
    }

    private void onReviewsLoaded(List<MovieReview> reviews) {
        mReviews = reviews;
        // Remove all existing reviews (everything but first two children)
        for (int i = mReviewsGroup.getChildCount() - 1; i >= 2; i--) {
            mReviewsGroup.removeViewAt(i);
        }

        final LayoutInflater inflater = LayoutInflater.from(MovieDetailActivity.this);
        boolean hasReviews = false;

        if (!reviews.isEmpty()) {
            for (MovieReview review : reviews) {
                if (TextUtils.isEmpty(review.getAuthor())) {
                    continue;
                }

                final View reviewView = inflater.inflate(R.layout.movie_review_detail, mReviewsGroup, false);
                final TextView reviewAuthorView = (TextView) reviewView.findViewById(R.id.review_author);
                final TextView reviewContentView = (TextView) reviewView.findViewById(R.id.review_content);

                reviewAuthorView.setText(review.getAuthor());
                reviewContentView.setText(review.getContent());

                mReviewsGroup.addView(reviewView);
                hasReviews = true;
            }
        }

        mReviewsGroup.setVisibility(hasReviews ? View.VISIBLE : View.GONE);
    }




    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        ViewCompat.setTranslationY(mCoverContainer, scrollY / 2);

        if (mToolbar != null) {
            int parallaxImageHeight = mCoverContainer.getMeasuredHeight();
            float alpha = Math.min(1, (float) scrollY / parallaxImageHeight);
            mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, mColorThemePrimary));
            mToolbar.setTitleTextColor(ScrollUtils.getColorWithAlpha(alpha, mColorTextWhite));
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
