package com.zacle.scheduler.ui.base;

import androidx.annotation.StringRes;

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */

public interface IView {

    void showMessage(String message);

    void showMessage(@StringRes int resId);

    void onError(String error);

    void onError(@StringRes int resId);

    boolean isNetworkConnected();

    void showSnackBar(String message);

    void showSnackBar(@StringRes int resId);
}
