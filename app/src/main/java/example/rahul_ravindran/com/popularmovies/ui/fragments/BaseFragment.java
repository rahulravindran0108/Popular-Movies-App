package example.rahul_ravindran.com.popularmovies.ui.fragments;

import android.app.Activity;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import example.rahul_ravindran.com.popularmovies.PopularMoviesApp;

/**
 * Created by rahulravindran on 27/12/15.
 */
public class BaseFragment extends Fragment {


    private Toast mToast;

    @CallSuper
    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @CallSuper
    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @CallSuper
    @Override public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @CallSuper
    @Override public void onDestroy() {

        super.onDestroy();
        PopularMoviesApp.get(getActivity()).getRefWatcher().watch(this);
    }

    protected void showToast(String message) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    protected void showToast(@StringRes int resId) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
