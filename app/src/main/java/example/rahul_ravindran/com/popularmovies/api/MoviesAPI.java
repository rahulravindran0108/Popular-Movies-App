package example.rahul_ravindran.com.popularmovies.api;



import java.util.List;

import example.rahul_ravindran.com.popularmovies.model.MovieReview;
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

    @GET("discover/movie")
    Observable<MovieDB.Response> discoverMovies(
            @Query("api_key") String api_key
            );

    @GET("discover/movie") Observable<MovieDB.Response> discoverMovies(
            @Query("api_key") String api_key,
            @Query("sort_by") Sort sort,
            @Query("page") int page);

    @GET("/discover/movie")
    Call<MovieDB.Response> discoverMovies(
            @Query("api_key") String api_key,
            @Query("sort_by") Sort sort,
            @Query("page") int page,
            @Query("include_adult") boolean includeAdult);

    @GET("movie/{id}/reviews") Observable<MovieReview.Response> getReview(
            @Path("id") long id,
            @Query("api_key") String api_key
    );
}
