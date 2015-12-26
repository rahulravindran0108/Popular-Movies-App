package example.rahul_ravindran.com.popularmovies.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.R;

/**
 * Created by rahulravindran on 17/12/15.
 */


public class MoviesAdapter extends EndlessRecylcerView<MovieDB, MoviesAdapter.MovieHolder> {

    private List<MovieDB> movies;
    private String baseImageUrl = "http://image.tmdb.org/t/p/w342/";
    private Context mContext;


    //interface to implement on click listener for movies
    public interface OnMovieClickListener {
        void onContentClicked(@NonNull final MovieDB movie, View view, int position);

        void onFavoredClicked(@NonNull final MovieDB movie, int position);

        OnMovieClickListener DUMMY = new OnMovieClickListener() {
            @Override public void onContentClicked(@NonNull MovieDB movie, View view, int position) {}

            @Override public void onFavoredClicked(@NonNull MovieDB movie, int position) { }
        };
    }

    @NonNull private OnMovieClickListener mListener = OnMovieClickListener.DUMMY;


    public void setListener(@NonNull OnMovieClickListener listener) {
        this.mListener = listener;
    }

    //constructor
    public MoviesAdapter(List<MovieDB> movies, Context c) {
        super(c,movies);
        this.movies = movies;
        this.mContext = c;
    }

    //override get item id
    @Override
    public long getItemId(int position) {
        return (!isLoadMore(position)) ? mItems.get(position).getId() : -1;
    }

    @Override
    protected MovieHolder onCreateItemHolder(ViewGroup parent, int viewType) {
        return new MovieHolder(mInflater.inflate(R.layout.movie_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ((MovieHolder) holder).bind(mItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    final class MovieHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.movie_item_container) View mContentContainer;
        @Bind(R.id.movie_item_image)ImageView mImageView;
        @Bind(R.id.movie_item_title)
        TextView mTitleView;
        @Bind(R.id.movie_item_genres) TextView mGenresView;
        @Bind(R.id.movie_item_footer) View mFooterView;
//        @Bind(R.id.movie_item_btn_favorite)
//        ImageButton mFavoriteButton;

        @BindColor(R.color.theme_primary) int mColorBackground;
        @BindColor(R.color.body_text_white) int mColorTitle;
        @BindColor(R.color.body_text_1_inverse) int mColorSubtitle;

        private long mMovieId;

        public MovieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bind(@NonNull final MovieDB movie) {
            mContentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onContentClicked(movie, view, MovieHolder.this.getAdapterPosition());
                }
            });

            mTitleView.setText(movie.getTitle());
            //mGenresView.setText(UiUtils.joinGenres(movie.getGenres(), ", ", mBuilder));

//            // prevents unnecessary color blinking
//            if (mMovieId != movie.getId()) {
//                resetColors();
//                mMovieId = movie.getId();
//            }
            Picasso.with(mContext).load(baseImageUrl+movie.getPosterPath()
            ).into(mImageView);
//            Glide.with(mContext)
//                    .load(movie.getPosterPath())
//                    .crossFade()
//                    .placeholder(R.color.movie_poster_placeholder)
//                    .listener(GlidePalette.with(movie.getPosterPath())
//                            .intoCallBack(palette -> applyColors(palette.getVibrantSwatch())))
//                    .into(mImageView);
        }

        private void resetColors() {
            mFooterView.setBackgroundColor(mColorBackground);
            mTitleView.setTextColor(mColorTitle);
            mGenresView.setTextColor(mColorSubtitle);
            //mFavoriteButton.setColorFilter(mColorTitle, PorterDuff.Mode.MULTIPLY);
        }

        private void applyColors(Palette.Swatch swatch) {
            if (swatch != null) {
                mFooterView.setBackgroundColor(swatch.getRgb());
                mTitleView.setTextColor(swatch.getBodyTextColor());
                mGenresView.setTextColor(swatch.getTitleTextColor());
                //mFavoriteButton.setColorFilter(swatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY);
            }
        }
    }
}
