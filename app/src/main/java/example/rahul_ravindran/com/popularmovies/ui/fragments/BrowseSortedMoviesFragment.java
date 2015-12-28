package example.rahul_ravindran.com.popularmovies.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.PopularMoviesApp;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.listeners.EndlessScrollListener;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by rahulravindran on 27/12/15.
 */
public class BrowseSortedMoviesFragment extends BrowseMoviesFragment implements EndlessScrollListener.OnLoadMoreCallback {

    private static final String ARG_SORT = "state_sort";

    private static final String STATE_CURRENT_PAGE = "state_current_page";
    private static final String STATE_IS_LOADING = "state_is_loading";

    private static final int VISIBLE_THRESHOLD = 10;
    private EndlessScrollListener mEndlessScrollListener;
    private BehaviorSubject<Observable<List<MovieDB>>> mItemsObservableSubject = BehaviorSubject.create();

    private Sort mSort;
    private int mCurrentPage = 0;
    private boolean mIsLoading = false;
    private MoviesRepoImpl mMoviesRepository;

    @Inject
    MoviesAPI apiService;

    public static BrowseSortedMoviesFragment newInstance(@NonNull Sort sort) {
        Bundle args = new Bundle();

        Log.i("popMovies",sort.toString());
        args.putSerializable(ARG_SORT, sort);

        BrowseSortedMoviesFragment fragment = new BrowseSortedMoviesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((PopularMoviesApp) getActivity().getApplication()).getDataProviderComponent().inject(this);
        mSort = (Sort) getArguments().getSerializable(ARG_SORT);

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE, 0);
            mIsLoading = savedInstanceState.getBoolean(STATE_IS_LOADING, true);
            Timber.d(String.format("Restoring state: pages 1-%d, was loading - %s", mCurrentPage, mIsLoading));
        }
        mMoviesRepository = new MoviesRepoImpl(apiService);
        newAdapter.setLoadMore(true);
        mViewAnimator.setDisplayedChildId((mCurrentPage == 0) ? ANIMATOR_VIEW_LOADING : ANIMATOR_VIEW_CONTENT);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeToMovies();
        if (savedInstanceState == null)
            reloadContent();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE, mCurrentPage);
        outState.putBoolean(STATE_IS_LOADING, mIsLoading);
        outState.putSerializable(ARG_SORT, mSort);
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (newAdapter.isLoadMore())
            pullPage(page);
    }

    protected final void reloadContent() {
        mViewAnimator.setDisplayedChildId(ANIMATOR_VIEW_LOADING);

        mSelectedPosition = -1;
        reAddOnScrollListener(mGridLayoutManager, mCurrentPage = 0);
        pullPage(1);
    }

    //getting the movies
    private void subscribeToMovies() {
        Timber.d("Subscribing to items");
        mSubscriptions.add(Observable.concat(mItemsObservableSubject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MovieDB>>() {
                    @Override
                    public void call(List<MovieDB> movies) {

                        mCurrentPage++;

                        Timber.d(String.format("Page %d is loaded, %d new items", mCurrentPage, movies.size()));
                        if (mCurrentPage == 1) newAdapter.clear();

                        newAdapter.setLoadMore(!movies.isEmpty());
                        newAdapter.add(movies);
                        mViewAnimator.setDisplayedChildId(ANIMATOR_VIEW_CONTENT);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Movies loading failed.");

                    }
                }));
    }

    private void pullPage(int page) {
        Timber.d(String.format("Page %d is loading.", page));

        mItemsObservableSubject.onNext(mMoviesRepository.discoverMovies(mSort, page));
    }

    private void reAddOnScrollListener(GridLayoutManager layoutManager, int startPage) {
        if (mEndlessScrollListener != null)
            movieList.removeOnScrollListener(mEndlessScrollListener);

        mEndlessScrollListener = EndlessScrollListener.fromGridLayoutManager(layoutManager, VISIBLE_THRESHOLD, startPage).setCallback(this);
        movieList.addOnScrollListener(mEndlessScrollListener);
    }


    @Override
    public void onFavoredClicked(@NonNull MovieDB movie, int position) {

    }
}
