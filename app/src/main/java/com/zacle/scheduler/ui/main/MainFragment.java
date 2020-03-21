package com.zacle.scheduler.ui.main;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseFragment;
import com.zacle.scheduler.viewmodel.ViewModelProviderFactory;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainFragment extends BaseFragment {

    private static final String TAG = "MainFragment";

    private MainViewModel viewModel;
    private Unbinder unbinder;
    private EventAdapter adapter;

    @BindView(R.id.schedules)
    public RecyclerView recyclerView;

    @Inject
    ViewModelProviderFactory providerFactory;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_main, container, false);
        setUp(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: created");

        setUp();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public void showSnackBar(String message) {

    }

    @Override
    public void showSnackBar(int resId) {

    }

    @Override
    protected void setUp(View view) {
        unbinder = ButterKnife.bind(this, view);
        setUnBinder(unbinder);
    }

    @Override
    protected void setUp() {
        initRecyclerView();
        subcribeObservers();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void subcribeObservers() {
        viewModel = ViewModelProviders.of(this, providerFactory).get(MainViewModel.class);

        viewModel.getFutureEvents().observe(this, resource -> {
            if (resource != null) {
                switch(resource.status) {
                    case LOADING:
                        Log.d(TAG, "subcribeObservers: LOADING...");
                        break;
                    case SUCCESS:
                        Log.d(TAG, "subcribeObservers: SUCCESS");
                        Log.d(TAG, "subcribeObservers: list length = " + resource.data.size());
                        adapter.submitList(resource.data);
                        break;
                    case ERROR:
                        Log.e(TAG, "subcribeObservers: ERROR: " + resource.message);
                        break;
                }
            }
        });
    }
}
