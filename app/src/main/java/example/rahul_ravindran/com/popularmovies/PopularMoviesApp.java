package example.rahul_ravindran.com.popularmovies;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.RefWatcher;

import java.util.Arrays;
import java.util.List;
import example.rahul_ravindran.com.popularmovies.api.APIModule;
import example.rahul_ravindran.com.popularmovies.api.DataProviderModule;
import example.rahul_ravindran.com.popularmovies.component.DataProviderComponent;
import example.rahul_ravindran.com.popularmovies.component.DaggerDataProviderComponent;
import example.rahul_ravindran.com.popularmovies.provider.MoviesProviderModule;
import timber.log.Timber;

/**
 * Created by rahulravindran on 25/12/15.
 */
public class PopularMoviesApp extends Application {

    private DataProviderComponent dataProviderComponent;
    private RefWatcher refWatcher;


    public static PopularMoviesApp get(Context context) {
        return (PopularMoviesApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher = installLeakCanary();

        Timber.plant(new Timber.DebugTree());

        initializeInjector();
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }

    protected RefWatcher installLeakCanary() {
        //return LeakCanary.install(this);
        return RefWatcher.DISABLED;
    }

    private void initializeInjector() {
        dataProviderComponent = DaggerDataProviderComponent.builder()
                .dataProviderModule(new DataProviderModule())
                .aPIModule(new APIModule())
                .moviesProviderModule(new MoviesProviderModule(this))
                .build();
    }

    public DataProviderComponent getDataProviderComponent() {
        return dataProviderComponent;
    }

}
