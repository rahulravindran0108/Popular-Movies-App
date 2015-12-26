package example.rahul_ravindran.com.popularmovies.repositories;

import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import example.rahul_ravindran.com.popularmovies.BuildConfig;
import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by rahulravindran on 24/12/15.
 */
public class MoviesRepoImpl implements MoviesRepo {

    private final MoviesAPI mMovieApi;

    public MoviesRepoImpl(MoviesAPI mMovieApi) {
        this.mMovieApi = mMovieApi;
    }

    @Override
    public Observable<List<MovieDB>> discoverMovies(Sort sort, int page) {
        Log.i("popMoviesCount",Integer.toString(page));
        return mMovieApi.discoverMovies(BuildConfig.API_KEY,sort,page)
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
        return mMovieApi.getReview(id,BuildConfig.API_KEY)
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


}
