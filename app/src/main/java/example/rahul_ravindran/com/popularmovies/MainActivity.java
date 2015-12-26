package example.rahul_ravindran.com.popularmovies;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import example.rahul_ravindran.com.popularmovies.adapters.MoviesAdapter;
import example.rahul_ravindran.com.popularmovies.api.DataProviderModule;
import example.rahul_ravindran.com.popularmovies.api.MoviesAPI;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.listeners.EndlessScrollListener;
import example.rahul_ravindran.com.popularmovies.repositories.MoviesRepoImpl;
import example.rahul_ravindran.com.popularmovies.ui.CustomViewAnimator;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements EndlessScrollListener.OnLoadMoreCallback,MoviesAdapter.OnMovieClickListener {


    protected GridLayoutManager mGridLayoutManager;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 10;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    protected MoviesAdapter newAdapter;
    protected int mSelectedPosition = -1;

    private Sort mSort;
    private BehaviorSubject<Observable<List<MovieDB>>> mItemsObservableSubject = BehaviorSubject.create();
    private MoviesRepoImpl mMoviesRepository;
    private static final int VISIBLE_THRESHOLD = 10;
    private int mCurrentPage = 0;
    private CompositeSubscription mSubscriptions;
    private EndlessScrollListener mEndlessScrollListener;
    private ModeSpinnerAdapter mSpinnerAdapter = new ModeSpinnerAdapter();
    Toolbar toolbar;
    private String mMode = Sort.popularity.toString();

    @Inject
    MoviesAPI apiService;

    @Bind(R.id.movies_recycler_view) RecyclerView movieList;
    @Bind(R.id.movies_animator)
    CustomViewAnimator mViewAnimator;

    protected static final int ANIMATOR_VIEW_LOADING = R.id.view_loading;
    protected static final int ANIMATOR_VIEW_CONTENT = R.id.movies_recycler_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ((PopularMoviesApp) getApplication()).getDataProviderComponent().inject(this);
        ButterKnife.bind(this);

        mSubscriptions = new CompositeSubscription();
        mSort = Sort.fromString(Sort.popularity.toString());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        List<MovieDB> movieDBList = new ArrayList<>();

        mMoviesRepository = new MoviesRepoImpl(apiService);

        newAdapter = new MoviesAdapter(movieDBList, getApplicationContext());


        newAdapter.setListener(this);

        newAdapter.setLoadMore(true);

        //set loading screen
        mViewAnimator.setDisplayedChildId((mCurrentPage == 0) ? ANIMATOR_VIEW_LOADING : ANIMATOR_VIEW_CONTENT);
        subscribeToMovies();
        initRecyclerView();
        initModeSpinner();




    }

    //reloads content for the grid view
    protected final void reloadContent() {
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


    @CallSuper
    protected void initRecyclerView() {

        if(MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mGridLayoutManager = new GridLayoutManager(MainActivity.this, getResources().getInteger(R.integer.movies_columns));
        }
        else{
            mGridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
        }

        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                int spanCount = mGridLayoutManager.getSpanCount();
                return (newAdapter.isLoadMore(position) /* && (position % spanCount == 0) */) ? spanCount : 1;
            }
        });

        movieList.setLayoutManager(mGridLayoutManager);
        newAdapter.setListener(MainActivity.this);
        movieList.setAdapter(newAdapter);
        if (mSelectedPosition != -1) movieList.scrollToPosition(mSelectedPosition);
        reAddOnScrollListener(mGridLayoutManager, mCurrentPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onLoadMore(int page, int totalItemsCount) {

        if (newAdapter.isLoadMore())
            pullPage(page);
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
    public void onContentClicked(@NonNull MovieDB movie, View view, int position) {
        Log.i("popMovies","clicked");
        Intent i = new Intent(this,MovieDetailActivity.class);
        i.putExtra("EXTRA_MOVIE", movie);
        startActivity(i);
    }

    @Override
    public void onFavoredClicked(@NonNull MovieDB movie, int position) {

    }

    //setting the mode spinner
    private void initModeSpinner() {

        if (toolbar == null)
            return;

        mSpinnerAdapter.clear();

        mSpinnerAdapter.addHeader("Filters");
        mSpinnerAdapter.addItem(Sort.popularity.toString(), getString(R.string.mode_sort_popularity), false);
        mSpinnerAdapter.addItem(Sort.vote_count.toString(), getString(R.string.mode_sort_vote_count), false);
        mSpinnerAdapter.addItem(Sort.vote_average.toString(), getString(R.string.mode_sort_vote_average), false);

        int itemToSelect = -1;


        if (mMode.equals(Sort.popularity.toString()))
            itemToSelect = 0;
        else if (mMode.equals(Sort.vote_count.toString()))
            itemToSelect = 1;
        else if (mMode.equals(Sort.vote_average.toString()))
            itemToSelect = 2;
        else
            itemToSelect = 0;

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.widget_toolbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.mode_spinner);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                onModeSelected(mSpinnerAdapter.getMode(position));
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        if (itemToSelect >= 0) {
            Timber.d("Restoring item selection to mode spinner: " + itemToSelect);
            spinner.setSelection(itemToSelect);
        }
    }

    private void onModeSelected(String mode) {
        if(mode == null || mode == "")
            mMode = Sort.popularity.toString();
        else
            mMode = mode;
        Log.i("popMovies",":"+mode);
        //
        mSort = Sort.fromString(mMode);
        reloadContent();

    }

    //mode spinner item
    private class ModeSpinnerItem {
        boolean isHeader;
        String mode, title;
        boolean indented;

        ModeSpinnerItem(boolean isHeader, String mode, String title, boolean indented) {
            this.isHeader = isHeader;
            this.mode = mode;
            this.title = title;
            this.indented = indented;
        }
    }

    //setting the mode spinner
    private class ModeSpinnerAdapter extends BaseAdapter {

        private ModeSpinnerAdapter() { }

        private ArrayList<ModeSpinnerItem> mItems = new ArrayList<ModeSpinnerItem>();

        public void clear() {
            mItems.clear();
        }

        public void addItem(String tag, String title, boolean indented) {
            mItems.add(new ModeSpinnerItem(false, tag, title, indented));
        }

        public void addHeader(String title) {
            mItems.add(new ModeSpinnerItem(true, "", title, false));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private boolean isHeader(int position) {
            return position >= 0 && position < mItems.size()
                    && mItems.get(position).isHeader;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
                view = getLayoutInflater().inflate(R.layout.item_toolbar_spinner_dropdown,
                        parent, false);
                view.setTag("DROPDOWN");
            }

            TextView headerTextView = (TextView) view.findViewById(R.id.header_text);
            View dividerView = view.findViewById(R.id.divider_view);
            TextView normalTextView = (TextView) view.findViewById(android.R.id.text1);

            if (isHeader(position)) {
                headerTextView.setText(getTitle(position));
                headerTextView.setVisibility(View.VISIBLE);
                normalTextView.setVisibility(View.GONE);
                dividerView.setVisibility(View.VISIBLE);
            } else {
                headerTextView.setVisibility(View.GONE);
                normalTextView.setVisibility(View.VISIBLE);
                dividerView.setVisibility(View.GONE);

                setUpNormalDropdownView(position, normalTextView);
            }

            return view;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
                view = getLayoutInflater().inflate(R.layout.item_toolbar_spinner, parent, false);
                view.setTag("NON_DROPDOWN");
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));
            return view;
        }

        private String getTitle(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).title : "";
        }

        private String getMode(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).mode : "";
        }

        private void setUpNormalDropdownView(int position, TextView textView) {
            textView.setText(getTitle(position));
        }

        @Override
        public boolean isEnabled(int position) {
            return !isHeader(position);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
    }
}
