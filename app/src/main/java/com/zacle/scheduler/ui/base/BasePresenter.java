package com.zacle.scheduler.ui.base;


/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * onAttach() and onDetach(). It also handles keeping a reference to the BaseView that
 * can be accessed from the children classes by calling getMvpView().
 */

public class BasePresenter<V extends BaseView> implements Presenter<V> {

    private static final String TAG = "BasePresenter";

    private V mView;

    public BasePresenter() {

    }

    @Override
    public void onAttach(V view) {
        this.mView = view;
    }

    @Override
    public void onDetach() {
        this.mView = null;
    }

    public V getView() {
        return this.mView;
    }
}
