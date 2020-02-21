package com.zacle.scheduler.ui.base;


/**
 * Every presenter in the app must either implement this interface or extend BasePresenter
 * indicating the IView type that wants to be attached with.
 */

public interface IPresenter<V extends IView> {

    void onAttach(V view);

    void onDetach();
}
