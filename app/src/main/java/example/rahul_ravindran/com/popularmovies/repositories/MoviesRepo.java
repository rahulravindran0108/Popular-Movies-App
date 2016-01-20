package example.rahul_ravindran.com.popularmovies.repositories;

import java.util.List;
import java.util.Map;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import example.rahul_ravindran.com.popularmovies.model.Video;
import rx.Observable;

/**
 * Created by rahulravindran on 24/12/15.
 */
public interface MoviesRepo {

    Observable<List<MovieDB>> discoverMovies(Sort sort, int page);

    Observable<List<MovieReview>> getMovieReview(long id);

    Observable<List<Genres>> getListOfGenres();

    Observable<List<Video>> videos(long movieId);

    void saveMovie(MovieDB movie);

    Observable<List<MovieDB>> savedMovies();

    void saveGenre(Genres genre);

    Observable<List<Genres>> savedGenres();
}
