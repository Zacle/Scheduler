package com.zacle.scheduler.ui.map;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.PolylineData;
import com.zacle.scheduler.service.geofence.GeofenceBroadcastReceiver;
import com.zacle.scheduler.service.location.LocationUpdatesService;
import com.zacle.scheduler.utils.AppConstants;
import com.zacle.scheduler.utils.GeofenceErrorMessages;
import com.zacle.scheduler.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.zacle.scheduler.utils.AppConstants.COURSE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.FINE_LOCATION;
import static com.zacle.scheduler.utils.AppConstants.LOCATION_PERMISSION_REQUEST_CODE;


public class RunningEventActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, OnCompleteListener<Void> {

    private static final String TAG = "RunningEventActivity";

    public static final String ID = "com.zacle.scheduler.ui.addOrEdit.id";
    public static final String NAME = "com.zacle.scheduler.ui.addOrEdit.name";
    public static final String DESTINATION_LAT = "com.zacle.scheduler.ui.addOrEdit.destinationLat";
    public static final String DESTINATION_LONG = "com.zacle.scheduler.ui.addOrEdit.destinationLong";


    private static final float DEFAULT_ZOOM = 17f;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int INIT = -1;

    private int id = INIT;
    private String name = "";
    private double destinationLat = INIT;
    private double destinationLong = INIT;
    private Boolean locationPermissionsGranted = false;

    private GoogleMap mMap;
    private LatLngBounds latLngBounds;
    private Marker marker;
    private Location myLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeoApiContext geoApiContext;
    private List<PolylineData> polylineDataList = new ArrayList<>();
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    public static Intent newIntent(Context context, int id, String name, double destinationLat, double destinationLong) {
        Intent intent = new Intent(context, RunningEventActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(NAME, name);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        return intent;
    }

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_running_event);

        bindService();

        verifyIntent();

        getLocationPermission();

        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    private void startLocationService() {
        Timber.d( "1. startLocationService: location tracking started...");

        Intent intent = LocationUpdatesService.newIntent(
                this,
                id,
                name,
                destinationLat,
                destinationLong
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            Timber.d( "3. startLocationService: location tracking started...");
            mService.requestLocationUpdates();
            RunningEventActivity.this.startForegroundService(intent);
        } else {
            Timber.d( "4. startLocationService: location tracking started...");
            startService(intent);
        }
    }

    // Bind to the service. If the service is in foreground mode, this signals to the service
    // that since this activity is in the foreground, the service can exit foreground mode.
    private void bindService() {
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    private void verifyIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(ID)) {
            id = intent.getIntExtra(ID, -1);
            name = intent.getStringExtra(NAME);
            destinationLat = intent.getDoubleExtra(DESTINATION_LAT, INIT);
            destinationLong = intent.getDoubleExtra(DESTINATION_LONG, INIT);
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

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnPolylineClickListener(this);
        }
    }

    private void getLocationPermission(){
        Timber.d( "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationPermissionsGranted = true;
                initMap();
                setGeofence();

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

    /*private void requestLocationUpdates() {
        Intent intent = LocationUpdatesService.newIntent(
                this,
                id,
                name,
                destinationLat,
                destinationLong
        );
        mService.requestLocationUpdates(intent);
    }*/

    private void initMap(){
        Timber.d( "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

        geofencingClient = LocationServices.getGeofencingClient(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d( "onRequestPermissionsResult: called.");
        locationPermissionsGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionsGranted = false;
                            Timber.d( "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Timber.d( "onRequestPermissionsResult: permission granted");
                    locationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void moveCamera(float zoom, String title){
        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        Timber.d( "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(options);
    }

    private void setCameraView() {
        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        double bottomBoundary = latLng.latitude - .1;
        double leftBoundary = latLng.longitude - .1;
        double topBoundary = latLng.latitude + .1;
        double rightBoundary = latLng.longitude + .1;

        latLngBounds = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private void displayEventPlace() {
        if (myLocation == null) {
            Toast.makeText(this, R.string.enable_location, Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        LatLng destinationLocation = new LatLng(destinationLat, destinationLong);

        MarkerOptions userOptions = new MarkerOptions()
                .position(userLocation);

        mMap.addMarker(userOptions);

        marker = mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .title(name));

        setCameraView();
    }

    private void getDeviceLocation() {
        Timber.d( "getDeviceLocation: getting the devices current location");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(locationPermissionsGranted) {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Timber.d( "onComplete: found location!");
                        myLocation = (Location) task.getResult();

                        displayEventPlace();
                        calculateDirections();
                        setLocationButtonClicked();
                        startLocationService();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void setLocationButtonClicked() {
        mMap.setOnMyLocationButtonClickListener(() -> {
            setCameraView();
            return true;
        });
    }

    private void calculateDirections() {
        Timber.d( "calculateDirections: calculating directions");

        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(myLocation.getLatitude(), myLocation.getLongitude())
        );

        directions.destination(new com.google.maps.model.LatLng(
                destinationLat,
                destinationLong
        ));

        directions.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Timber.d( "onResult: successfully retrieved directions.");
                Timber.d( "calculateDirections: routes: %s", result.routes[0].toString());
                Timber.d( "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Timber.d( "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Timber.d( "calculateDirections: geocodedWayPoints: %s", result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Timber.d( "run: result routes: " + result.routes.length);

            if (polylineDataList.size() > 0) {
                for (PolylineData polylineData: polylineDataList) {
                    polylineData.getPolyline().remove();
                }
                polylineDataList.clear();
                polylineDataList = new ArrayList<>();
            }

            double duration = 10000000;

            for (DirectionsRoute route: result.routes) {
                Timber.d( "run: leg: " + route.legs[0].toString());
                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                List<LatLng> newDecodedPath = new ArrayList<>();

                for (com.google.maps.model.LatLng latLng: decodedPath) {
                    newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                }

                Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                polyline.setColor(ContextCompat.getColor(this, R.color.mdtp_red));
                polyline.setClickable(true);
                polylineDataList.add(new PolylineData(polyline, route.legs[0]));

                // highlight the fastest route and adjust camera
                double tempDuration = route.legs[0].duration.inSeconds;
                if(tempDuration < duration){
                    duration = tempDuration;
                    onPolylineClick(polyline);
                    zoomRoute(polyline.getPoints());
                }
                marker.setVisible(false);
            }
        });
    }

    private void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: polylineDataList){
            index++;
            Timber.d( "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.colorAccent));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));


                marker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    private void setGeofence() {
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the geofence to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(buildGeofence());

        // Return a GeofencingRequest.
        return builder.build();
    }

    private Geofence buildGeofence() {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(id + "")
                .setCircularRegion(destinationLat, destinationLong, AppConstants.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(AppConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setLoiteringDelay(AppConstants.LOITERING_DELAY)
                .setNotificationResponsiveness(AppConstants.LOITERING_DELAY)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();

        return geofence;
    }

    /**
     * Gets a PendingIntent to send with the request to add Geofence. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {

            int messageId = R.string.geofence_added;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(RunningEventActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
