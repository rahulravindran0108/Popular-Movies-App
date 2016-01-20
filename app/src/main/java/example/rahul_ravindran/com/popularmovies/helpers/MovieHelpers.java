package example.rahul_ravindran.com.popularmovies.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import java.util.List;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.R;
import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.model.Video;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by rahulravindran on 26/12/15.
 */
public class MovieHelpers {

    Context mContext;
    Drawable mCurrentDrawable;
    private static final PublishSubject<FavoredEvent> FAVORED_SUBJECT = PublishSubject.create();
    private final MoviesRepoImpl mRepository;

    public MovieHelpers(Context c, MoviesRepoImpl mRepository) {
        this.mContext = c;
        this.mRepository = mRepository;
    }

    public void playVideo(Video video) {
        if (video.getSite().equals(Video.SITE_YOUTUBE)) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + video.getKey()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }

        else
            Timber.w("Unsupported video format");
    }


    public void setMovieFavored(MovieDB movie, boolean favored) {
        movie.setFavored(favored);
        if (favored) {
            mRepository.saveMovie(movie);
            mRepository.savedMovies()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<MovieDB>>() {
                        @Override
                        public void call(List<MovieDB> movies) {
                            Timber.d(String.format("Favored movies loaded, %d items", movies.size()));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.e(throwable, "Favored movies loading failed");

                        }
                    });
        }
        FAVORED_SUBJECT.onNext(new FavoredEvent(movie.getId(), favored));
    }

    public void setMovieGenres(List<Genres> genres) {
        for (Genres genre : genres) {
            mRepository.saveGenre(genre);
        }

        mRepository.savedGenres()
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new Action1<List<Genres>>() {
                    @Override
                    public void call(List<Genres> genres) {
                        Timber.d(String.format("genres loaded, %d items", genres.size()));
                        getGenres(genres);
                    }


                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "genres loading failed");

                    }
                });

    }

    public void getGenres(List<Genres> genres) {

    }

    public Drawable providesDrawable(int id) {
        switch(id) {
            case 28:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_action_movies);
                break;
            case 12:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_adventure_movies);
                break;
            case 16:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_animation_movies);
                break;
            case 35:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_comedy_movies);
                break;
            case 80:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_crime_movies);
                break;
            case 99:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_documentary_movies);
                break;
            case 18:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_drama_movies);
                break;
            case 10751:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_family_movies);
                break;
            case 14:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_fantasy_movies);
                break;
            case 10769:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_foreign_movies);
                break;
            case 36:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_history_movies);
                break;
            case 27:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_horror_movies);
                break;
            case 10402:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_music_movies);
                break;
            case 9648:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_mystery_movies);
                break;
            case 10749:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_romance_movies);
                break;
            case 878:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_science_fiction);
                break;
            case 53:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_thriller_movies);
                break;
            case 10752:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_war_movies);
                break;
            case 37:
                mCurrentDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_western_movies);
                break;
            default:
                mCurrentDrawable = ContextCompat.getDrawable(mContext,R.drawable.egg_empty);
                break;
        }

        return mCurrentDrawable;
    }

    public static class FavoredEvent {
        public long movieId;
        public boolean favored;

        private FavoredEvent(long movieId, boolean favored) {
            this.movieId = movieId;
            this.favored = favored;
        }
    }
}
