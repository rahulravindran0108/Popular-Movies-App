package example.rahul_ravindran.com.popularmovies.api;



import java.util.List;

import example.rahul_ravindran.com.popularmovies.model.Genres;
import example.rahul_ravindran.com.popularmovies.model.MovieReview;
import example.rahul_ravindran.com.popularmovies.model.Video;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import example.rahul_ravindran.com.popularmovies.MovieDB;
import rx.Observable;

/**
 * Created by rahulravindran on 07/12/15.
 */
public interface MoviesAPI {

    //get movies data
    @GET("discover/movie") Observable<MovieDB.Response> discoverMovies(
            @Query("api_key") String api_key,
            @Query("sort_by") Sort sort,
            @Query("page") int page);

    //get movies data with adult
    @GET("discover/movie")
    Observable<MovieDB.Response> discoverMovies(
            @Query("api_key") String api_key,
            @Query("sort_by") Sort sort,
            @Query("page") int page,
            @Query("include_adult") boolean includeAdult);

    //reviews get api
    @GET("movie/{id}/reviews") Observable<MovieReview.Response> getReview(
            @Path("id") long id,
            @Query("api_key") String api_key
    );

    //genres call
    @GET("genre/movie/list")
    Observable<Genres.Response> getGenres(
            @Query("api_key") String api_key
    );

    //get trailers
    @GET("movie/{id}/videos") Observable<Video.Response> videos(
            @Path("id") long movieId,
            @Query("api_key") String api_key);

}
