package example.rahul_ravindran.com.popularmovies.ui.activity;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import example.rahul_ravindran.com.popularmovies.PopularMoviesApp;
import example.rahul_ravindran.com.popularmovies.R;
import timber.log.Timber;

/**
 * Created by rahulravindran on 27/12/15.
 */
public class BaseTemplateActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;


    @CallSuper
    @Override protected void onDestroy() {
        super.onDestroy();
        PopularMoviesApp.get(this).getRefWatcher().watch(this);
    }

    @CallSuper
    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        configureToolbar();
    }

    @Nullable
    public final Toolbar getToolbar() {
        return mToolbar;
    }

    private void configureToolbar() {
        if (mToolbar == null) {
            Timber.w("Could not find the Required Toolbar");
            return;
        }

        //set elevation for toolbar
        ViewCompat.setElevation(mToolbar, getResources().getDimension(R.dimen.toolbar_elevation));
        setSupportActionBar(mToolbar);

        //setting up the action bar for adding menu items later
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
    }
}
