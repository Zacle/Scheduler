package com.zacle.scheduler.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

public abstract class BaseFragment extends DaggerFragment {

    private static final String TAG = "BaseFragment";

    private BaseActivity mBaseActivity;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public abstract void showMessage(String message);

    public abstract void showMessage(int resId);

    public abstract void onError(String error);

    public abstract void onError(int resId);

    public boolean isNetworkConnected() {
        return false;
    }

    public abstract void showSnackBar(String message);

    public abstract void showSnackBar(int resId);

    public BaseActivity getBaseActivity() {
        return mBaseActivity;
    }

    public void setUnBinder(Unbinder unBinder) {
        mUnbinder = unBinder;
    }

    protected abstract void setUp(View view);

    protected abstract void setUp();

    @Override
    public void onDestroy() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {

        void onFragmentAttached();

        void onFragmentDetached(String tag);
    }
}
