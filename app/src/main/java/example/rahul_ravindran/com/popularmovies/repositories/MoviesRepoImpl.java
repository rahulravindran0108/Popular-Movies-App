package example.rahul_ravindran.com.popularmovies.repositories;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.util.Log;

import com.squareup.sqlbrite.BriteContentResolver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import example.rahul_ravindran.com.popularmovies.BuildConfig;
import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import example.rahul_ravindran.com.popularmovies.model.Video;
import example.rahul_ravindran.com.popularmovies.provider.MoviesDBContract;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by rahulravindran on 24/12/15.
 */
public class MoviesRepoImpl implements MoviesRepo {

    private final MoviesAPI mMovieApi;
    private final ContentResolver mContentResolver;
    private final BriteContentResolver mBriteContentResolver;

    public MoviesRepoImpl(MoviesAPI mMovieApi,
                          ContentResolver mContentResolver,
                          BriteContentResolver mBriteContentResolver) {

        this.mMovieApi = mMovieApi;
        this.mContentResolver = mContentResolver;

        this.mBriteContentResolver = mBriteContentResolver;
    }

    @Override
    public Observable<List<MovieDB>> discoverMovies(Sort sort, int page) {
        Log.i("popMoviesCount",Integer.toString(page));
        return mMovieApi.discoverMovies(BuildConfig.API_KEY, sort, page)
                .timeout(5, TimeUnit.SECONDS)
                .retry(2)
                .map(new Func1<MovieDB.Response, List<MovieDB>>() {
                    @Override
                    public List<MovieDB> call(MovieDB.Response response) {
                        return response.movies;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<MovieReview>> getMovieReview(long id) {
        return mMovieApi.getReview(id, BuildConfig.API_KEY)
                .timeout(5,TimeUnit.SECONDS)
                .retry(2)
                .map(new Func1<MovieReview.Response, List<MovieReview>>() {
                    @Override
                    public List<MovieReview> call(MovieReview.Response response) {
                        return response.reviews;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Genres>> getListOfGenres() {
        return mMovieApi.getGenres(BuildConfig.API_KEY)
                .timeout(5,TimeUnit.SECONDS)
                .retry(2)
                .map(new Func1<Genres.Response, List<Genres>>() {
                    @Override
                    public List<Genres> call(Genres.Response response) {
                        return response.genres;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Video>> videos(long movieId) {
        return mMovieApi.videos(movieId, BuildConfig.API_KEY)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .map(new Func1<Video.Response, List<Video>>() {
                    @Override
                    public List<Video> call(Video.Response response) {
                        return response.videos;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<MovieDB>> savedMovies() {
        return mBriteContentResolver.createQuery(MoviesDBContract.MoviesDBHelper.CONTENT_URI,
                MovieDB.PROJECTION, null, null, MoviesDBContract.MoviesDBHelper.DEFAULT_SORT, true)
                .map(MovieDB.PROJECTION_MAP)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Genres>> savedGenres() {
        return mBriteContentResolver.createQuery(MoviesDBContract.GenresDBHelper.CONTENT_URI,
                Genres.PROJECTION, null, null, MoviesDBContract.GenresDBHelper.DEFAULT_SORT, true)
                .map(Genres.PROJECTION_LIST)
                .subscribeOn(Schedulers.io());
    }

    //save movies
    @Override
    public void saveMovie(MovieDB movie) {
        AsyncQueryHandler handler = new AsyncQueryHandler(mContentResolver) {};
        handler.startInsert(-1, null, MoviesDBContract.MoviesDBHelper.CONTENT_URI, new MovieDB.Builder()
                .movie(movie)
                .build());
    }


    //save genres
    @Override
    public void saveGenre(Genres genre) {

        AsyncQueryHandler handler = new AsyncQueryHandler(mContentResolver) {};
        handler.startInsert(-1,null,MoviesDBContract.GenresDBHelper.CONTENT_URI, new Genres.Builder().
                id((int) genre.getId()).name(genre.getName()).build());

    }



}
