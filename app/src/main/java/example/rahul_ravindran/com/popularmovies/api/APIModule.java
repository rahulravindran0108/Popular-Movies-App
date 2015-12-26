package example.rahul_ravindran.com.popularmovies.api;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import example.rahul_ravindran.com.popularmovies.BuildConfig;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Created by rahulravindran on 07/12/15.
 */
@Module
public class APIModule {

    @Provides @Singleton
    public OkHttpClient provideClient() {

        return new OkHttpClient();

    }

    @Provides @Singleton
    public Retrofit providesRetrofit(OkHttpClient client) {

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }
}
