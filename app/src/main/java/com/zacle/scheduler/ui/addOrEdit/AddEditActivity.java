package com.zacle.scheduler.ui.addOrEdit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseActivity;

public class AddEditActivity extends BaseActivity implements AddEditFragment.OnSaveEventListener {
    private static final String TAG = "AddEditActivity";

    public static final int ADD_EVENT_REQUEST = 1;
    public static final int UPDATE_EVENT_REQUEST = 2;

    private static final String ID = "com.zacle.scheduler.ui.addOrEdit.id";
    private static final String NAME = "com.zacle.scheduler.ui.addOrEdit.name";
    private static final String DATE = "com.zacle.scheduler.ui.addOrEdit.date";
    private static final String SOURCE_LAT = "com.zacle.scheduler.ui.addOrEdit.sourceLat";
    private static final String SOURCE_LONG = "com.zacle.scheduler.ui.addOrEdit.sourceLong";
    private static final String DESTINATION_LAT = "com.zacle.scheduler.ui.addOrEdit.destinationLat";
    private static final String DESTINATION_LONG = "com.zacle.scheduler.ui.addOrEdit.destinationLong";
    private static final String STATUS = "com.zacle.scheduler.ui.addOrEdit.status";
    private static final String NOTIFY = "com.zacle.scheduler.ui.addOrEdit.notify";
    private final int INIT = -1;

    private int id = INIT;
    private String name = "";
    private long date = INIT;
    private double sourceLat = INIT;
    private double sourceLong = INIT;
    private double destinationLat = INIT;
    private double destinationLong = INIT;
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


    public static Intent newIntent(Context context, int id, String name, long date, double sourceLat,
                                   double sourceLong, double destinationLat, double destinationLong, int status) {
        Intent intent = new Intent(context, AddEditActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(NAME, name);
        intent.putExtra(DATE, date);
        intent.putExtra(SOURCE_LAT, sourceLat);
        intent.putExtra(SOURCE_LONG, sourceLong);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        intent.putExtra(STATUS, status);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp();
        verifyIntent();
        startFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startFragment() {
        AddEditFragment addEditFragment;
        if (id != -1) {
            addEditFragment = AddEditFragment.newInstance(id, name, date, sourceLat, sourceLong, destinationLat, destinationLong, status);
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
            sourceLat = intent.getDoubleExtra(SOURCE_LAT, INIT);
            sourceLong = intent.getDoubleExtra(SOURCE_LONG, INIT);
            destinationLat = intent.getDoubleExtra(DESTINATION_LAT, INIT);
            destinationLong = intent.getDoubleExtra(DESTINATION_LONG, INIT);
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
    public void save(int id, String name, long date, double sourceLat, double sourceLong, double destinationLat, double destinationLong, int status) {

    }

    @Override
    public void save(String name, long date, double sourceLat, double sourceLong, double destinationLat, double destinationLong, int status) {

    }
}
