package com.zacle.scheduler.ui.base;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import butterknife.Unbinder;
import dagger.android.support.DaggerAppCompatActivity;

public abstract class BaseActivity extends DaggerAppCompatActivity implements BaseFragment.OnFragmentInteractionListener {

    private static final String TAG = "BaseActivity";

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
