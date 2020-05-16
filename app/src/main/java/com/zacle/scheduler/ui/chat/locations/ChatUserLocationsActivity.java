package com.zacle.scheduler.ui.chat.locations;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.ClusterManager;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.ClusterMarker;
import com.zacle.scheduler.data.model.UserLocation;
import com.zacle.scheduler.utils.SchedulerClusterManagerRender;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import timber.log.Timber;

import static com.zacle.scheduler.utils.AppConstants.COURSE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.FINE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.LOCATION_PERMISSION_REQUEST_CODE;

public class ChatUserLocationsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "ChatUserLocations";

    public static final String THREAD_NAME = "com.zacle.scheduler.ui.chat.locations.thread_name";
    public static final String USER_LOCATIONS = "com.zacle.scheduler.ui.chat.locations.user_locations";
    public static final String USER_LOCATIONS_BUNDLE = "com.zacle.scheduler.ui.chat.locations.user_locations_bundle";
    private static final float DEFAULT_ZOOM = 12f;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private String thread_name = "";
    private boolean locationPermissionsGranted = false;

    private GoogleMap mMap;
    private Thread thread;
    private FirebaseFirestore db;
    private List<User> users = new ArrayList<>();
    private List<UserLocation> userLocations = new ArrayList<>();
    private UserLocation userLocation;
    private ClusterManager<ClusterMarker> clusterManager;
    private SchedulerClusterManagerRender clusterManagerRender;
    private List<ClusterMarker> clusterMarkers = new ArrayList<>();
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    public static Intent newIntent(Context context, String name) {
        Intent intent = new Intent(context, ChatUserLocationsActivity.class);
        intent.putExtra(THREAD_NAME, name);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_user_locations);

        db = FirebaseFirestore.getInstance();

        verifyIntent();

        setUserLocation();

        getLocationPermission();
    }

    private void verifyIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(THREAD_NAME)) {
            thread_name = intent.getStringExtra(THREAD_NAME);
        }
        if (userLocations.size() == 0) {
            if (intent.hasExtra(USER_LOCATIONS)) {
                Bundle arg = intent.getBundleExtra(USER_LOCATIONS);
                final List<UserLocation> locations = arg.getParcelableArrayList(USER_LOCATIONS_BUNDLE);
                userLocations.addAll(locations);
            }
        }

        if (thread_name != null && !thread_name.isEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(thread_name);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable();
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationPermissionsGranted = true;
                initMap();

            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (locationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            addMarkers();
        }
    }

    private void initMap(){
        Timber.d("initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult: called.");
        locationPermissionsGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Timber.d("onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Timber.d("onRequestPermissionsResult: permission granted");
                    locationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void setUserLocation() {
        for (UserLocation location: userLocations) {
            if (location.getUser().getId().equals(ChatSDK.currentUser().getEntityID())) {
                Timber.d("Found user = %s", location.getUser().getUsername());
                userLocation = location;
            }
        }
    }

    private void setCameraView() {

        Log.d(TAG, "setCameraView: userLocation = " + userLocation);

        if (userLocation != null) {
            if (userLocation.getGeoPoint() != null) {
                LatLng latLng = new LatLng(
                        userLocation.getGeoPoint().getLatitude(),
                        userLocation.getGeoPoint().getLongitude()
                );

                double bottomBoundary = latLng.latitude - .1;
                double leftBoundary = latLng.longitude - .1;
                double topBoundary = latLng.latitude + .1;
                double rightBoundary = latLng.longitude + .1;

                LatLngBounds latLngBounds = new LatLngBounds(
                        new LatLng(bottomBoundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        }
    }

    private void addMarkers() {
        if (mMap != null) {
            if (clusterManager == null) {
                clusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);
            }
            if (clusterManagerRender == null) {
                clusterManagerRender = new SchedulerClusterManagerRender(
                        this,
                        mMap,
                        clusterManager
                );
                clusterManager.setRenderer(clusterManagerRender);
            }

            Timber.d("userLocations size = %d", userLocations.size());

            for (UserLocation location: userLocations) {
                Timber.d("user marker = %s", location.getUser().getUsername());
                Timber.d("user geopoint = %s", location.getGeoPoint());
                addUserMarker(location);
            }
            clusterManager.cluster();
            setCameraView();
        }
    }

    private void addUserMarker(UserLocation location) {
        ClusterMarker marker = new ClusterMarker(
                new LatLng(location.getGeoPoint().getLatitude(), location.getGeoPoint().getLongitude()),
                location.getUser().getUsername(),
                location.getDuration(),
                location.getUser()
        );
        clusterManager.addItem(marker);
        clusterMarkers.add(marker);
    }

    private void startUserLocationsRunnable() {
        Timber.d("startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = () -> {
            retrieveUserLocations();
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Timber.d("retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for (final ClusterMarker clusterMarker: clusterMarkers) {

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.users_location_collection))
                        .document(clusterMarker.getUser().getId());

                userLocationRef.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                        // update the location
                        for (int i = 0; i < clusterMarkers.size(); i++) {
                            try {
                                if (clusterMarkers.get(i).getUser().getId().equals(updatedUserLocation.getUser().getId())) {

                                    LatLng updatedLatLng = new LatLng(
                                            updatedUserLocation.getGeoPoint().getLatitude(),
                                            updatedUserLocation.getGeoPoint().getLongitude()
                                    );

                                    clusterMarkers.get(i).setPosition(updatedLatLng);
                                    clusterMarkers.get(i).setSnippet(updatedUserLocation.getDuration());
                                    clusterManagerRender.setUpdateMarker(clusterMarkers.get(i));
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        } catch (IllegalStateException e) {
            Timber.e("retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }
    }

}
