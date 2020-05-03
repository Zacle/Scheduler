package com.zacle.scheduler.ui.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.database.entity.Event;
import com.zacle.scheduler.service.alarm.Alarm;
import com.zacle.scheduler.service.alarm.EventAlarm;
import com.zacle.scheduler.ui.addOrEdit.AddEditActivity;
import com.zacle.scheduler.ui.base.BaseFragment;
import com.zacle.scheduler.ui.map.RunningEventActivity;
import com.zacle.scheduler.viewmodel.ViewModelProviderFactory;

import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;

import static android.app.Activity.RESULT_OK;
import static com.zacle.scheduler.ui.addOrEdit.AddEditActivity.DESTINATION_LAT;
import static com.zacle.scheduler.ui.addOrEdit.AddEditActivity.DESTINATION_LONG;
import static com.zacle.scheduler.ui.addOrEdit.AddEditActivity.NOTIFY;
import static com.zacle.scheduler.ui.addOrEdit.AddEditActivity.NOTIFY_SETTINGS;
import static com.zacle.scheduler.ui.addOrEdit.AddEditActivity.STATUS;
import static com.zacle.scheduler.utils.EventStatus.COMING;
import static com.zacle.scheduler.utils.EventStatus.RUNNING;

// TODO refactor
public class MainFragment extends BaseFragment implements EventAdapter.OnItemClickListener {

    private static final String TAG = "MainFragment";
    private static final String USER_AUTHENTICATED = "com.zacle.scheduler.ui.main.user_auth";

    private static final int ADD_EVENT_REQUEST = 1;
    private static final int UPDATE_EVENT_REQUEST = 2;
    public static final int RC_SIGN_IN = 900;

    private MainViewModel viewModel;
    private Unbinder unbinder;
    private EventAdapter adapter;

    @BindView(R.id.schedules)
    public RecyclerView recyclerView;
    @BindView(R.id.add_event)
    public FloatingActionButton floatingActionButton;

    @Inject
    ViewModelProviderFactory providerFactory;

    private OnFragmentInteractionListener mListener;

    // This is a list of extras that are passed to the login view
    protected HashMap<String, Object> extras = new HashMap<>();
    protected DisposableList disposableList = new DisposableList();

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
        Log.d(TAG, "onCreateView: created");
        setUp(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: created");

        setUp();
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
    public void onResume() {
        super.onResume();

        if (ChatSDK.auth().isAuthenticatedThisSession()) {
            Log.d(TAG, "onResume: Already authenticated");
        } else if (ChatSDK.auth().isAuthenticated()) {
            disposableList.add(ChatSDK.auth().authenticate()
                    .doFinally(() -> Log.d(TAG, "onResume: user = " + ChatSDK.currentUser().getEmail()))
                    .subscribe(() -> {}, throwable -> startLoginActivity()));
        } else {
            startLoginActivity();
        }
    }

    private void startLoginActivity() {
        Log.d(TAG, "startLoginActivity: starting login activity");
        startActivityForResult(ChatSDK.ui().getLoginIntent(getActivity(), extras), RC_SIGN_IN);
    }

    @Override
    public void onStop() {
        super.onStop();
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
        initFloatingActionButton();
        initRecyclerView();
        subscribeObservers();
        initSwipe();
    }

    private void initSwipe() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Event event = adapter.getEventAt(viewHolder.getAdapterPosition());

                viewModel.delete(event);

                Alarm alarm = new EventAlarm(getActivity(), event.getTime().getTime(), event.getNotification_time(), event.getNotification_settings());
                alarm.cancelAlarm(event.getId());

                Toast.makeText(getActivity(), "Event deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void initFloatingActionButton() {
        floatingActionButton.setOnClickListener(view -> {
            Intent intent = AddEditActivity.newIntent(getActivity());
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void subscribeObservers() {
        viewModel = ViewModelProviders.of(this, providerFactory).get(MainViewModel.class);
        Log.d(TAG, "subscribeObservers: initialized");

        viewModel.getFutureEvents().observe(this, resource -> {
            if (resource != null) {
                switch(resource.status) {
                    case LOADING:
                        Log.d(TAG, "subscribeObservers: LOADING...");
                        break;
                    case SUCCESS:
                        Log.d(TAG, "subscribeObservers: SUCCESS");
                        Log.d(TAG, "subscribeObservers: list length = " + resource.data.size());
                        adapter.submitList(resource.data);
                        break;
                    case ERROR:
                        Log.e(TAG, "subscribeObservers: ERROR: " + resource.message);
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int INIT = -1;

        if (requestCode == ADD_EVENT_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra(AddEditActivity.NAME);
            long date = data.getLongExtra(AddEditActivity.DATE, INIT);
            double destinationLat = data.getDoubleExtra(DESTINATION_LAT, INIT);
            double destinationLong = data.getDoubleExtra(DESTINATION_LONG, INIT);
            int status = data.getIntExtra(STATUS, 0);
            int notification_time = data.getIntExtra(NOTIFY, INIT);
            String notification_settings = data.getStringExtra(NOTIFY_SETTINGS);

            Event event = new Event(name, new Date(date), destinationLat, destinationLong, notification_time, notification_settings, false, COMING);
            viewModel.insert(event);

            Alarm alarm = new EventAlarm(getActivity(), date, notification_time, notification_settings);
            alarm.startAlarm(event.getId(), name);
            Toast.makeText(getActivity(), "Event inserted", Toast.LENGTH_SHORT).show();

        } else if (requestCode == UPDATE_EVENT_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(AddEditActivity.ID, INIT);
            String name = data.getStringExtra(AddEditActivity.NAME);
            long date = data.getLongExtra(AddEditActivity.DATE, INIT);
            double destinationLat = data.getDoubleExtra(DESTINATION_LAT, INIT);
            double destinationLong = data.getDoubleExtra(DESTINATION_LONG, INIT);
            int status = data.getIntExtra(STATUS, 0);
            int notification_time = data.getIntExtra(NOTIFY, INIT);
            String notification_settings = data.getStringExtra(NOTIFY_SETTINGS);

            Event event = new Event(name, new Date(date), destinationLat, destinationLong, notification_time, notification_settings, false, (status == 0) ? COMING : RUNNING);
            event.setId(id);
            viewModel.update(event);

            Alarm alarm = new EventAlarm(getActivity(), date, notification_time, notification_settings);
            alarm.editAlarm(event.getId(), name);

            Toast.makeText(getActivity(), "Event updated", Toast.LENGTH_SHORT).show();
        } else if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Toast.makeText(getActivity(), "Authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(Event event) {
        Intent intent = AddEditActivity.newIntent(getActivity(), event.getId(), event.getName(), event.getTime().getTime(),
                event.getDestinationLat(), event.getDestinationLong(), event.getNotification_time(),
                event.getNotification_settings(), COMING);

        startActivityForResult(intent, UPDATE_EVENT_REQUEST);
    }

    @Override
    public void onLunchClick(Event event) {

        if (serviceIsRunningInForeground(getActivity())) {
            Toast.makeText(getActivity(), "An event is already running", Toast.LENGTH_SHORT).show();
        } else {
            event.setStatus(RUNNING);
            viewModel.update(event);

            Intent intent = RunningEventActivity.newIntent(getActivity(), event.getId(), event.getName(),
                    event.getDestinationLat(), event.getDestinationLong());
            startActivity(intent);
        }
    }

    /**
     * Returns true if there is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        Log.d(TAG, "serviceIsRunningInForeground: checking... " + manager.getRunningServices(Integer.MAX_VALUE).size());
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if ("com.zacle.scheduler.service.location.LocationUpdatesService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
            Log.d(TAG, "serviceIsRunningInForeground: = " + service.service.getClassName());
        }
        return false;
    }
}
