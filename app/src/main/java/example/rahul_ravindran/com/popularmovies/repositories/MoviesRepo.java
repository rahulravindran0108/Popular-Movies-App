package example.rahul_ravindran.com.popularmovies.repositories;

import java.util.List;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import rx.Observable;

/**
 * Created by rahulravindran on 24/12/15.
 */
public interface MoviesRepo {

    Observable<List<MovieDB>> discoverMovies(Sort sort, int page);

    Observable<List<MovieReview>> getMovieReview(long id);
}
