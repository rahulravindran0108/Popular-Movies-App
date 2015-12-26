package example.rahul_ravindran.com.popularmovies.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Retrofit;

/**
 * Created by rahulravindran on 25/12/15.
 */
@Module
public class DataProviderModule {

    @Provides
    @Singleton
    MoviesAPI providesMoviesAPIService(Retrofit retrofit) {
        return retrofit.create(MoviesAPI.class);
    }


}
