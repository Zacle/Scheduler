package com.zacle.scheduler.ui.addOrEdit;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseFragment;
import com.zacle.scheduler.utils.AppConstants;
import com.zacle.scheduler.utils.DateUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class AddEditFragment extends BaseFragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, NotificationDialogFragment.OnInputSelected {
    private static final String TAG = "AddEditFragment";

    private static final String ID = "com.zacle.scheduler.ui.addOrEdit.id";
    private static final String NAME = "com.zacle.scheduler.ui.addOrEdit.name";
    private static final String DATE = "com.zacle.scheduler.ui.addOrEdit.date";
    private static final String DESTINATION_LAT = "com.zacle.scheduler.ui.addOrEdit.destinationLat";
    private static final String DESTINATION_LONG = "com.zacle.scheduler.ui.addOrEdit.destinationLong";
    private static final String NOTIFY = "com.zacle.scheduler.ui.addOrEdit.notify";
    public static final String NOTIFY_SETTINGS = "com.zacle.scheduler.ui.addOrEdit.notify_time";
    private static final String STATUS = "com.zacle.scheduler.ui.addOrEdit.status";

    private final int AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 771;
    private final int INIT = -1;

    private int id = INIT;
    private String name = "";
    private long date = INIT;
    private double destinationLat = INIT;
    private double destinationLong = INIT;
    private int status = 0;
    private int newHour = 0;
    private int newMinute = 0;
    private int newYear = INIT;
    private int newMonth = INIT;
    private int newDay = INIT;
    private int notification_time = 30;
    private String notification_settings = "minutes";
    private boolean destinationModified = false;

    private OnSaveEventListener saveEventListener;
    private Unbinder unbinder;
    private PlacesClient placesClient;
    private List<Place.Field> fields;
    private Intent intent;

    @BindView(R.id.name_text_input)
    public TextInputLayout name_text;

    @BindView(R.id.select_date)
    public Button selected_date;

    @BindView(R.id.select_time)
    public Button selected_time;

    @BindView(R.id.select_destination)
    public Button selected_destination;

    @BindView(R.id.add_notification)
    public Button notification;

    @BindView(R.id.completed)
    public CheckBox isCompleted;

    @BindView(R.id.save_event)
    public Button save;

    public AddEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static AddEditFragment newInstance(int id, String name, long date, double destinationLat, double destinationLong,
                                              int notification_time, String notification_settings, int status) {
        AddEditFragment fragment = new AddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        args.putString(NAME, name);
        args.putLong(DATE, date);
        args.putDouble(DESTINATION_LAT, destinationLat);
        args.putDouble(DESTINATION_LONG, destinationLong);
        args.putInt(NOTIFY, notification_time);
        args.putString(NOTIFY_SETTINGS, notification_settings);
        args.putInt(STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            id = bundle.getInt(ID);
            name = bundle.getString(NAME);
            date = bundle.getLong(DATE);
            destinationLat = bundle.getDouble(DESTINATION_LAT);
            destinationLong = bundle.getDouble(DESTINATION_LONG);
            notification_time = bundle.getInt(NOTIFY);
            notification_settings = bundle.getString(NOTIFY_SETTINGS);
            status = bundle.getInt(STATUS);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_event, container, false);
        setUp(view);
        setUp();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSaveEventListener) {
            saveEventListener = (OnSaveEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSaveEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        saveEventListener = null;
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
        Log.d(TAG, "showSnackBar: showing snackbar message...");
        Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSnackBar(int resId) {
        Log.d(TAG, "showSnackBar: showing snackbar resId...");
        Snackbar.make(getActivity().findViewById(android.R.id.content), resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void setUp(View view) {
        unbinder = ButterKnife.bind(this, view);
        setUnBinder(unbinder);
        initUI();
    }

    private void initUI() {
        if (id != INIT) {
            name_text.getEditText().setText(name);
            selected_date.setText(DateUtil.formatDate(date));
            selected_time.setText(DateUtil.formatTime(date));
            notification.setText(notification_time + " " + getTimePlural(notification_settings) + " before");
            setDestinationLocation();
            setDate();
            setTime();
        }
    }

    private void setDate() {
        String time = DateUtil.formatDate(date);
        Date formated = DateUtil.parseDate(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formated);
        newYear = calendar.get(Calendar.YEAR);
        newMonth = calendar.get(Calendar.MONTH);
        newDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void setTime() {
        String time = DateUtil.formatTime(date);
        Date formated = DateUtil.parseTime(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formated);
        newHour = calendar.get(Calendar.HOUR_OF_DAY);
        newMinute = calendar.get(Calendar.MINUTE);
    }

    private void setDestinationLocation() {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(destinationLat, destinationLong, 1);
            Address address = addresses.get(0);
            String location = address.getAddressLine(0);
            selected_destination.setText(location);
        } catch(IOException e) {
            Log.e(TAG, "Geocoder I/O Exception", e);
        }
    }

    @Override
    protected void setUp() {
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
            datePickerDialog.setTitle("Select Event date");
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
        String date = "You picked the following date: " + dayOfMonth + "/" + (monthOfYear+1) + "/" + year;
        newYear = year;
        newMonth = monthOfYear;
        newDay = dayOfMonth;
        Date d = DateUtil.getDate(year, monthOfYear, dayOfMonth, 0, 0);
        selected_date.setText(DateUtil.formatDate(d.getTime()));
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        String time = "You picked the following time: " + hourString + "h" + minuteString + "m" + secondString + "s";
        newHour = hourOfDay;
        newMinute = minute;
        selected_time.setText(hourString + ":" + minuteString);
    }

    @OnClick(R.id.select_destination)
    public void searchDestinationLocation() {
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINATION);
    }

    @OnClick(R.id.add_notification)
    public void setNotificationTime() {
        NotificationDialogFragment notificationDialogFragment = NotificationDialogFragment.newInstance(notification_time, notification_settings);
        notificationDialogFragment.setTargetFragment(AddEditFragment.this, 1);
        notificationDialogFragment.show(getActivity().getSupportFragmentManager(), "NotificationDialogFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selected_destination.setText(place.getAddress());
                LatLng latLng = place.getLatLng();
                destinationLat = latLng.latitude;
                destinationLong = latLng.longitude;
                destinationModified = true;
                Log.i(TAG, "Place: " + place.getAddress() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.save_event)
    public void saveEvent() {
        if (!checkAndSaveName()) {
            showSnackBar(R.string.enter_name);
            return;
        }
        if (!checkAndSaveDate()) {
            showSnackBar(R.string.select_date_or_time);
            return;
        }
        if (!checkDestination()) {
            showSnackBar(R.string.select_destination_location);
            return;
        }

        if (id == INIT) {
            saveEventListener.save(name, date, destinationLat, destinationLong, status, notification_time, notification_settings);
        } else {
            saveEventListener.save(id, name, date, destinationLat, destinationLong, status, notification_time, notification_settings);
        }

    }

    @Override
    public void sendInput(String time, String time_settings) {

        try {
            notification_time = Integer.parseInt(time);
        } catch (NumberFormatException nf) {
            Log.d(TAG, "sendInput: parse exception");
        }
        notification_settings = time_settings;

        notification.setText(time + " " + getTimePlural(time_settings) + " before");
    }

    private String getTimePlural(String time) {
        if (time.equals(AppConstants.MINUTES)) {
            return (notification_time == 1) ? "minute" : time;
        }
        return (notification_time == 1) ? "hour" : time;
    }

    private boolean checkAndSaveName() {
        name = name_text.getEditText().getText().toString();
        return !name.equals("");
    }

    private boolean checkAndSaveDate() {
        if (id != INIT && newYear == INIT) {
            return false;
        }
        long validate = DateUtil.getDate(newYear, newMonth, newDay, newHour, newMinute).getTime();
        long currentTime = new Date().getTime();
        if (validate < currentTime) {
            return false;
        }
        date = validate;
        return true;
    }

    private boolean checkDestination() {
        if (id != INIT) {
            return true;
        }
        return destinationModified;
    }

    public interface OnSaveEventListener {
        void save(int id, String name, long date, double destinationLat, double destinationLong, int status,
                  int notification_time, String notification_settings);

        void save(String name, long date, double destinationLat, double destinationLong, int status,
                         int notification_time, String notification_settings);
    }
}
