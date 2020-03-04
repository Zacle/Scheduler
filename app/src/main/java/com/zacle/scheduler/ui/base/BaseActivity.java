package com.zacle.scheduler.ui.base;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity implements BaseView, BaseFragment.OnFragmentInteractionListener {

    private static final String TAG = "BaseActivity";

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public void showMessage(int resId) {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onError(int resId) {

    }

    @Override
    public boolean isNetworkConnected() {
        return false;
    }

    @Override
    public void showSnackBar(String message) {

    }

    @Override
    public void showSnackBar(int resId) {

    }

    public void setUnbinder(Unbinder unbinder) {
        mUnbinder = unbinder;
    }

    @Override
    protected void onDestroy() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        super.onDestroy();
    }

    protected abstract void setUp();
}
