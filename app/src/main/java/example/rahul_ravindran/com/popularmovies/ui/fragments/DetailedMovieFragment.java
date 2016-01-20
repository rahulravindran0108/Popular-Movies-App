package example.rahul_ravindran.com.popularmovies.ui.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.BriteContentResolver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.OnClick;
import example.rahul_ravindran.com.popularmovies.MovieDB;

import example.rahul_ravindran.com.popularmovies.PopularMoviesApp;
import example.rahul_ravindran.com.popularmovies.R;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.helpers.MovieHelpers;
import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import example.rahul_ravindran.com.popularmovies.model.Video;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import example.rahul_ravindran.com.popularmovies.ui.activity.MovieDetailActivity;
import example.rahul_ravindran.com.popularmovies.ui.helpers.CircleTransform;
import example.rahul_ravindran.com.popularmovies.ui.widgets.ResizableImageView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

/**
 * Created by rahulravindran on 27/12/15.
 */
public class DetailedMovieFragment extends BaseFragment implements ObservableScrollViewCallbacks {


    public static final String ARG_MOVIE = "arg_movie";

    private static final String STATE_SCROLL_VIEW = "state_scroll_view";
    private static final String STATE_REVIEWS = "state_reviews";
    private static final String STATE_VIDEOS = "state_trailers";

    @Nullable
    Toolbar mToolbar;
    @Bind(R.id.movie_scroll_view)
    ObservableScrollView mScrollView;
    @Bind(R.id.movie_cover_container)
    FrameLayout mCoverContainer;
    @Bind(R.id.movie_cover)
    ResizableImageView coverImageView;
    @Bind(R.id.movie_poster)
    ResizableImageView moviePosterImageView;
    @Bind(R.id.movie_title)
    TextView movieTitle;
    @Bind(R.id.movie_release_date)
    TextView movieReleaseDate;
    @Bind(R.id.movie_average_rating)
    TextView movieAverageRating;
    @Bind(R.id.movie_overview)
    TextView movieOverview;
    @Bind(R.id.ratingBars)
    RatingBar movieRatingBar;
    @Bind(R.id.movie_reviews_container)
    ViewGroup mReviewsGroup;
    @Bind(R.id.movie_videos_container)
    ViewGroup mVideosGroup;
    @Bind(R.id.movie_genres_container)
    ViewGroup mGenresGroup;
    @Bind(R.id.movie_poster_play)
    ImageView mPosterPlayImage;

    @Bind(R.id.drawable_movies_popularity)
    TextView mDrawableMoviesPopularity;
    @Bind(R.id.drawable_movies_rating)
    TextView mDrawableMoviesRating;
    @Bind(R.id.drawable_movies_vote_count)
    TextView mDrawableMoviesVoteCount;

    @BindColor(R.color.theme_primary)
    int mColorThemePrimary;
    @BindColor(R.color.body_text_white)
    int mColorTextWhite;

    private String baseImageUrl = "http://image.tmdb.org/t/p/w342/";
    private CompositeSubscription mSubscriptions;
    private MoviesRepoImpl mMoviesRepository;
    private MovieDB mMovie;
    private List<MovieReview> mReviews;
    private List<Genres> mGenres;
    private List<Video> mVideos;
    private MovieHelpers mHelpers;
    private Video mTrailer;
    private List<Runnable> mDeferredUiOperations = new ArrayList<>();
    private MenuItem mMenuItemShare;


    @Inject
    MoviesAPI mApiService;

    @Inject
    ContentResolver mContentResolver;

    @Inject
    BriteContentResolver mBriteContentResolver;


    public static DetailedMovieFragment newInstance(MovieDB movie) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        Log.i("popMovies", movie.getTitle());
        DetailedMovieFragment fragment = new DetailedMovieFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((PopularMoviesApp) getActivity().getApplication()).getDataProviderComponent().inject(this);
        trySetupToolbar();
        mScrollView.setScrollViewCallbacks(this);

        if (savedInstanceState != null) {
            mVideos = savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
            mReviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
            mScrollView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_SCROLL_VIEW));
        }
    }

    @OnClick(R.id.movie_favorite_button)
    public void onFavored(ImageButton button) {
        if (mMovie == null) return;

        boolean favored = !mMovie.isFavored();
        button.setSelected(favored);
        mHelpers.setMovieFavored(mMovie, favored);
        if (favored) showToast(R.string.message_movie_favored);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        mMoviesRepository = new MoviesRepoImpl(mApiService, mContentResolver, mBriteContentResolver);
        mHelpers = new MovieHelpers(getContext(), mMoviesRepository);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);

        onMovieLoaded((MovieDB) getArguments().getParcelable(ARG_MOVIE));

        if (mReviews != null) onReviewsLoaded(mReviews);
        else loadReviews();

        if (mVideos != null) onVideosLoaded(mVideos);
        else loadVideos();

        if (mGenres != null) onGenresLoaded(mGenres);
        else loadGenres();
//        loadReviews();
//        loadVideos();
//        loadGenres();

    }

    private void loadVideos() {
        mSubscriptions.add(mMoviesRepository.videos(mMovie.getId()).subscribe(new Action1<List<Video>>() {
            @Override
            public void call(final List<Video> videos) {
                Timber.d(String.format("Videos loaded, %d items.", videos.size()));
                Timber.d("Videos: " + videos);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onVideosLoaded(videos);
                    }
                });


            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Timber.e(throwable, "Videos loading failed.");
                //onVideosLoaded(null);
            }
        }));
    }

    private void onVideosLoaded(List<Video> videos) {
        mVideos = videos;

        // Remove all existing videos (everything but first two children)
        for (int i = mVideosGroup.getChildCount() - 1; i >= 2; i--) {
            mVideosGroup.removeViewAt(i);
        }

        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        boolean hasVideos = false;
        if (!videos.isEmpty()) {
            for (Video video : mVideos)
                if (video.getType().equals(Video.TYPE_TRAILER)) {
                    Timber.d("Found trailer!");
                    mTrailer = video;

                    mCoverContainer.setTag(video);
                    mCoverContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mHelpers.playVideo((Video) view.getTag());
                        }
                    });
                    break;
                }

            for (Video video : videos) {
                final View videoView = inflater.inflate(R.layout.item_video, mVideosGroup, false);
                final TextView videoNameView = findById(videoView, R.id.video_name);

                videoNameView.setText(video.getSite() + ": " + video.getName());
                videoView.setTag(video);
                videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHelpers.playVideo((Video) v.getTag());
                    }
                });

                mVideosGroup.addView(videoView);
                hasVideos = true;
            }
        }

        showShareMenuItemDeferred(mTrailer != null);
        mCoverContainer.setClickable(mTrailer != null);
        mPosterPlayImage.setVisibility(mTrailer != null ? View.VISIBLE : View.GONE);
        mVideosGroup.setVisibility(hasVideos ? View.VISIBLE : View.GONE);
    }

    private void showShareMenuItemDeferred(final boolean visible) {
        mDeferredUiOperations.add(new Runnable() {
            @Override
            public void run() {
                mMenuItemShare.setVisible(visible);
            }
        });
        tryExecuteDeferredUiOperations();
    }

    private void tryExecuteDeferredUiOperations() {
        if (mMenuItemShare != null) {
            for (Runnable r : mDeferredUiOperations) {
                r.run();
            }
            mDeferredUiOperations.clear();
        }
    }

    private void onMovieLoaded(MovieDB movie) {
        Log.i("popMoviesDetail", movie.getTitle());
        mMovie = movie;

        Picasso.with(getContext()).load(baseImageUrl +
                mMovie.getBackdropPath()).into(coverImageView);


        Picasso.with(getContext()).load(baseImageUrl +
                mMovie.getBackdropPath()).transform(new CircleTransform()).
                into(moviePosterImageView);

        movieTitle.setText(mMovie.getTitle());
        movieReleaseDate.setText("Release Date:" + mMovie.getReleaseDate());
        movieAverageRating.setText("Average Rating:" + Double.toString(mMovie.getVoteAverage()));
        movieOverview.setText(mMovie.getOverview());
        movieRatingBar.setNumStars(10);
        movieRatingBar.setRating((float) mMovie.getVoteAverage());

        //setting drawable text

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        mDrawableMoviesPopularity.setText(df.format(mMovie.getPopularity()));
        mDrawableMoviesRating.setText(df.format(mMovie.getVoteAverage()));
        mDrawableMoviesVoteCount.setText(Long.toString(mMovie.getVoteCount()));


    }

    //loading genres
    private void loadGenres() {
        mSubscriptions.add(mMoviesRepository.getListOfGenres()
                        .subscribe(
                                new Action1<List<Genres>>() {
                                    @Override
                                    public void call(final List<Genres> genres) {

                                        Timber.d(String.format("Genres loaded, %d items.", genres.size()));
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                onGenresLoaded(genres);
                                            }
                                        });

                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Timber.e(throwable, "Genres loading failed.");
                                        //onReviewsLoaded(null);
                                    }
                                }
                        )
        );
    }

    //loading reviews
    private void loadReviews() {
        mSubscriptions.add(mMoviesRepository.getMovieReview(mMovie.getId())
                .subscribe(new Action1<List<MovieReview>>() {
                    @Override
                    public void call(final List<MovieReview> reviews) {

                        Timber.d(String.format("Reviews loaded, %d items.", reviews.size()));

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onReviewsLoaded(reviews);
                            }
                        });


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Reviews loading failed.");
                        //onReviewsLoaded(null);
                    }
                }));
    }

    private void onGenresLoaded(List<Genres> genres) {
        //set movie genres
        mHelpers.setMovieGenres(genres);
        mGenres = genres;
        List<Integer> movieGenreID = mMovie.getGenreIds();


        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        boolean hasGenres = false;

        if (!genres.isEmpty()) {
            for (int genreId : movieGenreID) {

                final View genreView = inflater.inflate(R.layout.movie_genre_detail,
                        mReviewsGroup, false);

                final ImageView genreImageView = findById(genreView, R.id.movie_genre_icon);

                genreImageView.setImageDrawable(mHelpers.providesDrawable(genreId));

                mGenresGroup.addView(genreView);
                hasGenres = true;

            }
        }

        mGenresGroup.setVisibility(hasGenres ? View.VISIBLE : View.GONE);

    }

    private void onReviewsLoaded(List<MovieReview> reviews) {
        mReviews = reviews;
        // Remove all existing reviews (everything but first two children)
        for (int i = mReviewsGroup.getChildCount() - 1; i >= 2; i--) {
            mReviewsGroup.removeViewAt(i);
        }

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        boolean hasReviews = false;


        if (!reviews.isEmpty()) {
            for (MovieReview review : reviews) {
                if (TextUtils.isEmpty(review.getAuthor())) {
                    continue;
                }

                final View reviewView = inflater.inflate(R.layout.movie_review_detail, mReviewsGroup, false);
                final TextView reviewAuthorView = findById(reviewView, R.id.review_author);
                final TextView reviewContentView = findById(reviewView, R.id.review_content);

                reviewAuthorView.setText(review.getAuthor());
                reviewContentView.setText(review.getContent());

                mReviewsGroup.addView(reviewView);
                hasReviews = true;


            }
        }

        mReviewsGroup.setVisibility(hasReviews ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail, menu);
        mMenuItemShare = menu.findItem(R.id.menu_share);
        tryExecuteDeferredUiOperations();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_SCROLL_VIEW, mScrollView.onSaveInstanceState());
        if (mReviews != null)
            outState.putParcelableArrayList(STATE_REVIEWS, new ArrayList<>(mReviews));
        if (mVideos != null)
            outState.putParcelableArrayList(STATE_VIDEOS, new ArrayList<>(mVideos));
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        super.onDestroyView();
    }

    private void trySetupToolbar() {
        if (getActivity() instanceof MovieDetailActivity) {
            MovieDetailActivity activity = ((MovieDetailActivity) getActivity());
            mToolbar = activity.getToolbar();
        }
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
}
