package example.rahul_ravindran.com.popularmovies.ui.fragments;

import android.content.ContentResolver;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.sqlbrite.BriteContentResolver;

import java.util.List;

import javax.inject.Inject;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.PopularMoviesApp;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

public class FavoredMoviesFragment extends BrowseMoviesFragment {

    private Subscription mFavoredSubscription = Subscriptions.empty();
    private MoviesRepoImpl mMoviesRepository;

    @Inject
    MoviesAPI apiService;

    @Inject
    ContentResolver mContentResolver;

    @Inject
    BriteContentResolver mBriteContentResolver;

    @Override
    public void onStop() {
        super.onStop();
        mFavoredSubscription.unsubscribe();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PopularMoviesApp) getActivity().getApplication()).getDataProviderComponent().inject(this);
        subscribeToMovies();
    }

    private void subscribeToMovies() {
        mMoviesRepository = new MoviesRepoImpl(apiService, mContentResolver, mBriteContentResolver);
        mViewAnimator.setDisplayedChildId(ANIMATOR_VIEW_LOADING);
        mFavoredSubscription.unsubscribe();

        mFavoredSubscription = mMoviesRepository.savedMovies().
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Action1<List<MovieDB>>() {
                    @Override
                    public void call(List<MovieDB> movieDBs) {
                        Log.i("Popular Movies App", new Integer(movieDBs.size()).toString());
                        for(MovieDB movies : movieDBs) {
                            Log.i("movie name",movies.getTitle());
                        }
                        newAdapter.add(movieDBs);
                        newAdapter.notifyDataSetChanged();
                        mViewAnimator.setDisplayedChildId(getContentView());


                    }
                });
    }

    @IdRes
    protected final int getContentView() {
        return  ANIMATOR_VIEW_CONTENT;
    }

    @Override
    public void onFavoredClicked(@NonNull MovieDB movie, int position) {

    }
}
