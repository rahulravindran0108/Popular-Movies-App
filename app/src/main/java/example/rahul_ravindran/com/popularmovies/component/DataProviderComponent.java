package example.rahul_ravindran.com.popularmovies.component;

import javax.inject.Singleton;

import dagger.Component;

import example.rahul_ravindran.com.popularmovies.api.APIModule;
import example.rahul_ravindran.com.popularmovies.api.DataProviderModule;
import example.rahul_ravindran.com.popularmovies.ui.fragments.BrowseSortedMoviesFragment;
import example.rahul_ravindran.com.popularmovies.ui.fragments.DetailedMovieFragment;

/**
 * Created by rahulravindran on 26/12/15.
 */
@Singleton
@Component(modules={APIModule.class, DataProviderModule.class})
public interface DataProviderComponent {
    void inject(BrowseSortedMoviesFragment fragment);
    void inject(DetailedMovieFragment fragment);
}