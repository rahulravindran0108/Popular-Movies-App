package example.rahul_ravindran.com.popularmovies.ui.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.R;
import example.rahul_ravindran.com.popularmovies.adapters.MoviesAdapter;
import example.rahul_ravindran.com.popularmovies.listeners.EndlessScrollListener;
import example.rahul_ravindran.com.popularmovies.ui.widgets.CustomViewAnimator;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by rahulravindran on 27/12/15.
 */
public abstract class BrowseMoviesFragment extends BaseFragment implements
        MoviesAdapter.OnMovieClickListener{

    public interface Listener {
        void onMovieSelected(MovieDB movie, View view);
    }

    protected static final int ANIMATOR_VIEW_LOADING = R.id.view_loading;
    protected static final int ANIMATOR_VIEW_CONTENT = R.id.movies_recycler_view;

    @Bind(R.id.movies_recycler_view) RecyclerView movieList;
    @Bind(R.id.movies_animator) CustomViewAnimator mViewAnimator;

    protected GridLayoutManager mGridLayoutManager;
    protected Listener listener;
    protected CompositeSubscription mSubscriptions;
    protected MoviesAdapter newAdapter;
    protected int mSelectedPosition = -1;
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_SELECTED_POSITION = "state_selected_position";

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement MoviesFragment.Listener.");
        }

        super.onAttach(activity);
        listener = (Listener) activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeSubscription();

        mSelectedPosition = savedInstanceState != null
                ? savedInstanceState.getInt(STATE_SELECTED_POSITION, -1)
                : -1;

        List<MovieDB> movieDBList = new ArrayList<>();

        newAdapter = new MoviesAdapter(movieDBList, getActivity());
        newAdapter.setListener(this);


        initRecyclerView();
    }

    @CallSuper
    protected void initRecyclerView() {
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mGridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.movies_columns));
        }
        else{
            mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        }

        //mGridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.movies_columns));
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                int spanCount = mGridLayoutManager.getSpanCount();
                return (newAdapter.isLoadMore(position) /* && (position % spanCount == 0) */) ? spanCount : 1;
            }
        });

        movieList.setLayoutManager(mGridLayoutManager);
        movieList.setAdapter(newAdapter);
        if (mSelectedPosition != -1) movieList.scrollToPosition(mSelectedPosition);
    }

    @Override
    public void onContentClicked(@NonNull MovieDB movie, View view, int position) {
        mSelectedPosition = position;
        listener.onMovieSelected(movie, view);
    }

    @Override
    public void onDetach() {
        listener = new Listener() {
            @Override
            public void onMovieSelected(MovieDB movie, View view) {
            }
        };
        newAdapter.setListener(MoviesAdapter.OnMovieClickListener.DUMMY);
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_MOVIES, new ArrayList<>(newAdapter.getItems()));
        outState.putInt(STATE_SELECTED_POSITION, mSelectedPosition);
    }
}
