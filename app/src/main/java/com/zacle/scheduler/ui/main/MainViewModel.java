package com.zacle.scheduler.ui.main;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    @Inject
    public MainViewModel() {
        Log.d(TAG, "MainViewModel: mainviewmodel is ready...");
    }
}
