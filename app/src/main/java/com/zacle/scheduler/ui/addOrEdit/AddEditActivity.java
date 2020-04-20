package com.zacle.scheduler.ui.addOrEdit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseActivity;
import com.zacle.scheduler.utils.EventStatus;

public class AddEditActivity extends BaseActivity implements AddEditFragment.OnSaveEventListener {
    private static final String TAG = "AddEditActivity";

    public static final String ID = "com.zacle.scheduler.ui.addOrEdit.id";
    public static final String NAME = "com.zacle.scheduler.ui.addOrEdit.name";
    public static final String DATE = "com.zacle.scheduler.ui.addOrEdit.date";
    public static final String DESTINATION_LAT = "com.zacle.scheduler.ui.addOrEdit.destinationLat";
    public static final String DESTINATION_LONG = "com.zacle.scheduler.ui.addOrEdit.destinationLong";
    public static final String STATUS = "com.zacle.scheduler.ui.addOrEdit.status";
    public static final String NOTIFY = "com.zacle.scheduler.ui.addOrEdit.notify";
    public static final String NOTIFY_SETTINGS = "com.zacle.scheduler.ui.addOrEdit.notify_time";
    private final int INIT = -1;

    private int id = INIT;
    private String name = "";
    private long date = INIT;
    private double destinationLat = INIT;
    private double destinationLong = INIT;
    private int notification_time = 30;
    private String notification_settings = "minutes";
    private int status = 0;

    /**
     * Called to create a new event
     * @param context
     * @return
     */
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AddEditActivity.class);
        return intent;
    }


    public static Intent newIntent(Context context, int id, String name, long date, double destinationLat, double destinationLong,
                                   int notification_time, String notification_settings, EventStatus status) {
        Intent intent = new Intent(context, AddEditActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(NAME, name);
        intent.putExtra(DATE, date);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        intent.putExtra(NOTIFY, notification_time);
        intent.putExtra(NOTIFY_SETTINGS, notification_settings);
        intent.putExtra(STATUS, status.getCode());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp();
        verifyIntent();
        if (id != INIT) {
            getSupportActionBar().setTitle(R.string.edit_event);
        } else {
            getSupportActionBar().setTitle(R.string.add_event);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startFragment() {
        AddEditFragment addEditFragment;
        if (id != -1) {
            addEditFragment = AddEditFragment.newInstance(id, name, date, destinationLat, destinationLong,
                                                            notification_time, notification_settings, status);
        } else {
            addEditFragment = new AddEditFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_add_container, addEditFragment).commit();
    }

    private void verifyIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(ID)) {
            id = intent.getIntExtra(ID, -1);
            name = intent.getStringExtra(NAME);
            date = intent.getLongExtra(DATE, 0);
            destinationLat = intent.getDoubleExtra(DESTINATION_LAT, INIT);
            destinationLong = intent.getDoubleExtra(DESTINATION_LONG, INIT);
            notification_time = intent.getIntExtra(NOTIFY, 30);
            notification_settings = intent.getStringExtra(NOTIFY_SETTINGS);
            status = intent.getIntExtra(STATUS, 0);
        }
    }

    @Override
    protected void setUp() {
        setContentView(R.layout.activity_add_or_edit);
    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    public void save(int id, String name, long date, double destinationLat, double destinationLong, int status, int notification_time, String notification_settings) {
        Intent intent = new Intent();
        intent.putExtra(ID, id);
        intent.putExtra(NAME, name);
        intent.putExtra(DATE, date);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        intent.putExtra(STATUS, status);
        intent.putExtra(NOTIFY, notification_time);
        intent.putExtra(NOTIFY_SETTINGS, notification_settings);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void save(String name, long date, double destinationLat, double destinationLong, int status, int notification_time, String notification_settings) {
        Intent intent = new Intent();
        intent.putExtra(NAME, name);
        intent.putExtra(DATE, date);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        intent.putExtra(STATUS, status);
        intent.putExtra(NOTIFY, notification_time);
        intent.putExtra(NOTIFY_SETTINGS, notification_settings);

        setResult(RESULT_OK, intent);
        finish();
    }
}
