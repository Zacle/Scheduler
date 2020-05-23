package com.zacle.scheduler.ui.main;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.User;
import com.zacle.scheduler.data.model.UserLocation;
import com.zacle.scheduler.ui.base.BaseActivity;

import co.chatsdk.core.session.ChatSDK;
import timber.log.Timber;

import static com.zacle.scheduler.utils.AppConstants.COURSE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.ERROR_DIALOG_REQUEST;
import static com.zacle.scheduler.utils.AppConstants.FINE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.zacle.scheduler.utils.AppConstants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public static final String ID = "com.zacle.scheduler.ui.main.id";

    private boolean locationPermissionGranted = false;

    private User user;
    private UserLocation userLocation;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        if (checkMapServices()) {
            if (!locationPermissionGranted) {
                getLocationPermission();
            } else {
                saveUser();
            }
        }

        Intent intent = getIntent();
        int notificationId = intent.getIntExtra(ID, -1);
        if (notificationId != -1) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }

        setUp();
    }

    private void saveUser() {
        if (userLocation == null) {
            co.chatsdk.core.dao.User currentUser = ChatSDK.currentUser();
            if (currentUser != null) {
                user = new User(currentUser.getEntityID(), currentUser.getName(), currentUser.getAvatarURL());
                userLocation = new UserLocation();

                DocumentReference userRef = db.collection(getString(R.string.users_collection))
                        .document(user.getId());

                userRef.set(user)
                        .addOnSuccessListener(aVoid -> {
                            Timber.tag(TAG).d("DocumentSnapshot successfully written!");
                            userLocation.setUser(user);
                            getLastKnownLocation();
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, "saveUser: Failed to write new user to firestore: " + e.getMessage());
                        });
            }
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProvider.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            userLocation.setGeoPoint(geoPoint);
            userLocation.setTime(null);
            saveUserLocation();
        });

    }

    private void saveUserLocation(){

        if(userLocation != null){
            DocumentReference locationRef = db
                    .collection(getString(R.string.users_location_collection))
                    .document(user.getId());

            locationRef.set(userLocation).addOnCompleteListener(task -> {
                Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                        "\n latitude: " + userLocation.getGeoPoint().getLatitude() +
                        "\n longitude: " + userLocation.getGeoPoint().getLongitude());
            });
        }
    }

    @Override
    protected void setUp() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chats:
                ChatSDK.ui().startMainActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    private boolean checkMapServices() {
        if (isServiceOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void showMessage(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
    }

    private boolean isServiceOK() {
        Timber.tag(TAG).d("isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Timber.tag(TAG).d("isServicesOK: Google Play Services is working");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Timber.tag(TAG).d("isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            showMessage(R.string.no_map_service);
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps_requirement))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alarm_ok_button), (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        Timber.tag(TAG).d("getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationPermissionGranted = true;
                saveUser();

            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(locationPermissionGranted){
                    saveUser();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
}