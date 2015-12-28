package example.rahul_ravindran.com.popularmovies.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import example.rahul_ravindran.com.popularmovies.MovieDB;
import example.rahul_ravindran.com.popularmovies.ui.activity.MovieDetailActivity;
import example.rahul_ravindran.com.popularmovies.R;
import example.rahul_ravindran.com.popularmovies.api.Sort;
import example.rahul_ravindran.com.popularmovies.ui.fragments.BrowseMoviesFragment;
import example.rahul_ravindran.com.popularmovies.ui.fragments.BrowseSortedMoviesFragment;
import example.rahul_ravindran.com.popularmovies.ui.fragments.DetailedMovieFragment;
import timber.log.Timber;

/**
 * Created by rahulravindran on 27/12/15.
 */
public class MoviesBrowserActivity extends BaseTemplateActivity implements BrowseMoviesFragment.Listener{

    private boolean twoPanes;
    private BrowseMoviesFragment mMoviesFragment;
    private ModeSpinnerAdapter mSpinnerAdapter = new ModeSpinnerAdapter();
    private String mMode = Sort.popularity.toString();

    //to find the movies by fragment name
    private static final String MOVIE_FRAGMENT_TAG = "movies_fragment";
    private static final String MOVIE_DETAILS_FRAGMENT_TAG = "movies_detail_fragment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        twoPanes = findViewById(R.id.movie_details_container) != null;
        Log.i("popMovies", Boolean.toString(twoPanes));
        initModeSpinner();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void initModeSpinner() {
        Toolbar toolbar = getToolbar();
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

        replaceMoviesFragment(BrowseSortedMoviesFragment.newInstance(Sort.fromString(mMode)));

//        if (mMode.equals(MODE_FAVORITES))
//            replaceMoviesFragment(new FavoredMoviesFragment());
//        else
//            replaceMoviesFragment(SortedMoviesFragment.newInstance(Sort.fromString(mMode)));
    }

    private void replaceMoviesFragment(BrowseMoviesFragment fragment) {
        mMoviesFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movies_container, fragment, MOVIE_FRAGMENT_TAG)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .commit();
    }

    @Override
    public void onMovieSelected(MovieDB movie, View view) {

        Timber.d(String.format("Movie '%s' selected", movie.getTitle()));

        if (twoPanes) {
            DetailedMovieFragment fragment = DetailedMovieFragment.newInstance(movie);
            replaceMovieDetailsFragment(fragment);
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
            startActivity(intent);
        }

    }

    private void replaceMovieDetailsFragment(DetailedMovieFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_details_container, fragment, MOVIE_DETAILS_FRAGMENT_TAG)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .commit();
    }

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
