package example.rahul_ravindran.com.popularmovies;

import android.app.Application;

import java.util.Arrays;
import java.util.List;
import example.rahul_ravindran.com.popularmovies.api.APIModule;
import example.rahul_ravindran.com.popularmovies.api.DataProviderModule;
import example.rahul_ravindran.com.popularmovies.component.DataProviderComponent;
import example.rahul_ravindran.com.popularmovies.component.DaggerDataProviderComponent;

/**
 * Created by rahulravindran on 25/12/15.
 */
public class PopularMoviesApp extends Application {

    private DataProviderComponent dataProviderComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
    }

    private void initializeInjector() {
        dataProviderComponent = DaggerDataProviderComponent.builder()
                .dataProviderModule(new DataProviderModule())
                .aPIModule(new APIModule())
                .build();
    }

    public DataProviderComponent getDataProviderComponent() {
        return dataProviderComponent;
    }

}
