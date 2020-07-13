package com.zacle.scheduler.ui.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.database.entity.Event;
import com.zacle.scheduler.service.alarm.Alarm;
import com.zacle.scheduler.service.alarm.EventAlarm;
import com.zacle.scheduler.ui.addOrEdit.AddEditEvent;
import com.zacle.scheduler.ui.addOrEdit.NotificationDialogFragment;
import com.zacle.scheduler.ui.base.BaseFragment;
import com.zacle.scheduler.ui.map.RunningEventActivity;
import com.zacle.scheduler.utils.DateUtil;
import com.zacle.scheduler.utils.DeleteDialogFragment;
import com.zacle.scheduler.viewmodel.ViewModelProviderFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.zacle.scheduler.utils.EventStatus.COMING;
import static com.zacle.scheduler.utils.EventStatus.RUNNING;

// TODO refactor
public class MainFragment extends BaseFragment implements EventAdapter.OnItemClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, NotificationDialogFragment.OnInputSelected,
        DeleteDialogFragment.DeleteDialogListener {

    private static final String TAG = "MainFragment";


    public static final int RC_SIGN_IN = 900;
    private static final int AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 770;

    private MainViewModel viewModel;
    private Unbinder unbinder;
    private EventAdapter adapter;

    @BindView(R.id.schedules)
    public RecyclerView recyclerView;
    @BindView(R.id.add_event)
    public FloatingActionButton floatingActionButton;
    @BindView(R.id.touch_outside)
    public View touch_outside;

    // Bottom Sheet initialization
    @BindView(R.id.main_bottom_sheet)
    public LinearLayout main_bottom_sheet;

    @BindView(R.id.name_text_input)
    public EditText name_text;

    @BindView(R.id.select_date)
    public Button selected_date;

    @BindView(R.id.select_time)
    public Button selected_time;

    @BindView(R.id.select_destination)
    public Button selected_destination;

    @BindView(R.id.add_notification)
    public Button notification;

    @BindView(R.id.save_event)
    public Button save;

    @BindView(R.id.cancel_event_bottom_sheet)
    public Button cancel;

    private BottomSheetBehavior sheetBehavior;

    // End Bottom Sheet initialization

    @Inject
    ViewModelProviderFactory providerFactory;

    private OnFragmentInteractionListener mListener;
    private AddEditEvent addEditEvent;
    private PlacesClient placesClient;
    private List<Place.Field> fields;
    private Intent intent;

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
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int resId) {
        Toast.makeText(getActivity(), getString(resId), Toast.LENGTH_SHORT).show();
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
        Snackbar.make(getActivity().findViewById(android.R.id.content), resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void setUp(View view) {
        unbinder = ButterKnife.bind(this, view);
        setUnBinder(unbinder);
    }

    @Override
    protected void setUp() {
        initBottomSheet();
        initFloatingActionButton();
        initRecyclerView();
        subscribeObservers();
    }

    private void initBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(main_bottom_sheet);

        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        floatingActionButton.setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        touch_outside.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        floatingActionButton.setVisibility(View.VISIBLE);
                        resetUI();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                touch_outside.setVisibility(View.VISIBLE);
                touch_outside.setAlpha(slideOffset);
            }
        });
    }

    private void initFloatingActionButton() {
        floatingActionButton.setOnClickListener(view -> {
            initPlace();
            initDateDialog();
            initTimeDialog();
            addEditEvent = new AddEditEvent(getActivity());
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
    }

    private void resetUI() {
        name_text.setText("");
        selected_date.setText(getActivity().getString(R.string.select_date));
        selected_time.setText(getActivity().getString(R.string.select_time));
        notification.setText(getActivity().getString(R.string.notification));
        selected_destination.setText(getActivity().getString(R.string.select_destination));
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
        Timber.d("subscribeObservers: initialized");

        viewModel.getFutureEvents().observe(this, resource -> {
            if (resource != null) {
                switch(resource.status) {
                    case LOADING:
                        Timber.d("subscribeObservers: LOADING...");
                        break;
                    case SUCCESS:
                        Timber.d("subscribeObservers: SUCCESS");
                        Timber.d("subscribeObservers: list length = %d", resource.data.size());
                        adapter.initExpandable(resource.data.size());
                        adapter.submitList(resource.data);
                        break;
                    case ERROR:
                        Timber.e("subscribeObservers: ERROR: %s", resource.message);
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int INIT = -1;

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            showMessage(R.string.authenticated);
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selected_destination.setText(place.getAddress());
                LatLng latLng = place.getLatLng();
                addEditEvent.setDestination(latLng.latitude, latLng.longitude, true);
                Log.i(TAG, "Place: " + place.getAddress() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Timber.i(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private void initUI() {
        name_text.setText(addEditEvent.getName());
        selected_date.setText(DateUtil.formatDate(addEditEvent.getDate()));
        selected_time.setText(DateUtil.formatTime(addEditEvent.getDate()));
        notification.setText(addEditEvent.getNotificationText());
        selected_destination.setText(addEditEvent.getDestinationLocation());
        initPlace();
        initDateDialog();
        initTimeDialog();
    }

    private void initPlace() {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), apiKey);
        }
        placesClient = Places.createClient(getActivity());
        fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getActivity());
    }

    // TODO replace with Material Design
    private void initDateDialog() {
        selected_date.setOnClickListener(view -> {
            Calendar now = Calendar.getInstance();

            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.dismissOnPause(true);
            datePickerDialog.showYearPickerFirst(false);
            datePickerDialog.setTitle(getString(R.string.select_event_date));
            datePickerDialog.show(getActivity().getSupportFragmentManager(), "DatePickerDialog");
        });
    }

    // TODO replace with Material Design
    private void initTimeDialog() {
        selected_time.setOnClickListener(view -> {
            Calendar now = Calendar.getInstance();

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show(getActivity().getSupportFragmentManager(), "TimePickerDialog");
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        addEditEvent.setDate(year, monthOfYear, dayOfMonth);
        Date d = DateUtil.getDate(year, monthOfYear, dayOfMonth, 0, 0);
        selected_date.setText(DateUtil.formatDate(d.getTime()));
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        addEditEvent.setTime(hourOfDay, minute);
        selected_time.setText(hourString + ":" + minuteString);
    }

    @OnClick(R.id.select_destination)
    public void searchDestinationLocation() {
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINATION);
    }

    @OnClick(R.id.add_notification)
    public void setNotificationTime() {
        NotificationDialogFragment notificationDialogFragment = NotificationDialogFragment.newInstance(
                addEditEvent.getNotification_time(),
                addEditEvent.getNotification_settings()
        );
        notificationDialogFragment.setTargetFragment(MainFragment.this, 1);
        notificationDialogFragment.show(getActivity().getSupportFragmentManager(), "NotificationDialogFragment");
    }

    @OnClick(R.id.save_event)
    public void saveEvent() {
        if (!addEditEvent.checkAndSaveName(name_text.getText().toString())) {
            showSnackBar(R.string.enter_name);
            return;
        }
        if (!addEditEvent.checkAndSaveDate()) {
            showSnackBar(R.string.select_date_or_time);
            return;
        }
        if (!addEditEvent.checkDestination()) {
            showSnackBar(R.string.select_destination_location);
            return;
        }

        if (addEditEvent.isNewEvent()) {
            save(addEditEvent.getName(), addEditEvent.getDate(), addEditEvent.getDestinationLat(),
                    addEditEvent.getDestinationLong(), addEditEvent.getStatus(),
                    addEditEvent.getNotification_time(), addEditEvent.getNotification_settings());
        } else {
            save(addEditEvent.getId(), addEditEvent.getName(), addEditEvent.getDate(), addEditEvent.getDestinationLat(),
                    addEditEvent.getDestinationLong(), addEditEvent.getStatus(),
                    addEditEvent.getNotification_time(), addEditEvent.getNotification_settings());
        }
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.cancel_event_bottom_sheet)
    public void cancelEvent() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.touch_outside)
    public void clickOutside() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void save(String name, long date, double destinationLat, double destinationLong, int status,
                      int notification_time, String notification_settings) {
        Event event = new Event(name, new Date(date), destinationLat, destinationLong, notification_time, notification_settings, false, COMING);
        viewModel.insert(event);

        Alarm alarm = new EventAlarm(getActivity(), date, notification_time, notification_settings);
        alarm.startAlarm(event.getId(), name);
        showMessage(R.string.event_inserted);
    }

    private void save(int id, String name, long date, double destinationLat, double destinationLong, int status,
                      int notification_time, String notification_settings) {
        Event event = new Event(name, new Date(date), destinationLat, destinationLong, notification_time, notification_settings, false, (status == 0) ? COMING : RUNNING);
        event.setId(id);
        viewModel.update(event);

        Alarm alarm = new EventAlarm(getActivity(), date, notification_time, notification_settings);
        alarm.editAlarm(event.getId(), name);

        showMessage(R.string.event_updated);
    }

    @Override
    public void onLunchClick(Event event) {

        if (serviceIsRunningInForeground(getActivity())) {
            showMessage(R.string.event_already_running);
        } else {
            event.setStatus(RUNNING);
            viewModel.update(event);

            Intent intent = RunningEventActivity.newIntent(getActivity(), event.getId(), event.getName(),
                    event.getDestinationLat(), event.getDestinationLong());
            startActivity(intent);
        }
    }

    @Override
    public void onEditClick(Event event) {
        addEditEvent = new AddEditEvent(getActivity(), event.getId(), event.getName(), event.getTime().getTime(),
                event.getDestinationLat(), event.getDestinationLong(), event.getNotification_time(),
                event.getNotification_settings(), event.getStatus().getCode());

        initUI();
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDeleteClick(Event event) {
        openDialog(event);
    }

    private void openDialog(Event event) {
        DeleteDialogFragment dialog = new DeleteDialogFragment();
        dialog.setEvent(event);
        dialog.setTargetFragment(MainFragment.this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "");
    }

    /**
     * Returns true if there is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Timber.d("serviceIsRunningInForeground: checking... %s", manager.getRunningServices(Integer.MAX_VALUE).size());
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if ("com.zacle.scheduler.service.location.LocationUpdatesService".equals(service.service.getClassName())) {
                Timber.d("isLocationServiceRunning: location service is already running.");
                return true;
            }
            Timber.d("serviceIsRunningInForeground: = %s", service.service.getClassName());
        }
        return false;
    }

    @Override
    public void sendInput(String time, String time_settings) {
        try {
            addEditEvent.setNotification_time(Integer.parseInt(time));
        } catch (NumberFormatException nf) {
            Timber.e("sendInput: parse exception");
        }
        addEditEvent.setNotification_settings(time_settings);

        notification.setText(addEditEvent.getNotificationText());
    }

    @Override
    public void onYesClicked(Event event) {
        viewModel.delete(event);

        Alarm alarm = new EventAlarm(getActivity(), event.getTime().getTime(), event.getNotification_time(), event.getNotification_settings());
        alarm.cancelAlarm(event.getId());

        showMessage(R.string.event_deleted);
    }
}