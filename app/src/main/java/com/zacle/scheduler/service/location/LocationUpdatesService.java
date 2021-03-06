package com.zacle.scheduler.service.location;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.User;
import com.zacle.scheduler.data.model.UserLocation;
import com.zacle.scheduler.ui.map.RunningEventActivity;
import com.zacle.scheduler.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.chatsdk.core.session.ChatSDK;
import timber.log.Timber;


/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service {

    private static final String PACKAGE_NAME = "com.zacle.scheduler.service.location.LocationUpdatesService";
    public static final String TAG = LocationUpdatesService.class.getName();
    public static final String ID = PACKAGE_NAME + ".id";
    public static final String NAME = PACKAGE_NAME + ".name";
    public static final String DESTINATION_LAT = PACKAGE_NAME + ".destinationLat";
    public static final String DESTINATION_LONG = PACKAGE_NAME + ".destinationLong";

    private final String CHANNEL_ID = "channel_01";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder binder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    private GeoApiContext geoApiContext;

    /**
     * The current location.
     */
    private Location mLocation;

    private String notificationText = "";

    private static final int INIT = 123456789;

    private int id = INIT;
    private String name = "";
    private double destinationLat = INIT;
    private double destinationLong = INIT;

    public LocationUpdatesService() {
    }

    public static Intent newIntent(Context context, int id, String name, double destinationLat, double destinationLong) {
        Intent intent = new Intent(context, LocationUpdatesService.class);
        intent.putExtra(ID, id);
        intent.putExtra(NAME, name);
        intent.putExtra(DESTINATION_LAT, destinationLat);
        intent.putExtra(DESTINATION_LONG, destinationLong);
        return intent;
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            mChannel.setSound(null, null);
            mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            int notificationId = intent.getIntExtra(ID, -1);
            if (notificationId != -1) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notificationId);
            }
            stopSelf();
        }

        verifyIntent(intent);

        startForeground(NOTIFICATION_ID, getNotification());

        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    private void verifyIntent(Intent intent) {
        if (intent.hasExtra(ID)) {
            id = intent.getIntExtra(ID, INIT);
            name = intent.getStringExtra(NAME);
            destinationLat = intent.getDoubleExtra(DESTINATION_LAT, INIT);
            destinationLong = intent.getDoubleExtra(DESTINATION_LONG, INIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (RunningEventActivity in this case) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (RunningEventActivity in this case) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (RunningEventActivity in this case) unbinds from this
        // service. If this method is called due to a configuration change in RunningEventActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service, id = " + id + ", name = " + name);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Log.d(TAG, "startLocationService: location tracking started...");
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        if (notificationText.equals("")) {
            locationAddress(mLocation);
        }

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        intent.putExtra(ID, id);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                RunningEventActivity.newIntent(this, id, name, destinationLat, destinationLong), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(notificationText)
                .setContentTitle(Utils.getLocationTitle(this))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(notificationText)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        locationAddress(location);

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }

        updatePosition(location);
    }

    private void locationAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            notificationText = "Current location: " + address.getAddressLine(0);
        } catch(IOException e) {
            Timber.e("Geocoder I/O Exception: %s", e.getMessage());
        }
    }

    private void updatePosition(Location location) {
            // - Perform the actual request
            DirectionsApiRequest directions = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    ))
                    .destination(new com.google.maps.model.LatLng(
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
                    String durationText;
                    // - Parse the result
                    DirectionsRoute route = result.routes[0];
                    DirectionsLeg leg = route.legs[0];
                    Duration duration = leg.duration;
                    durationText = duration.humanReadable;

                    durationText = "Remaining " + durationText;

                    saveUserLocation(location, durationText, duration.inSeconds);
                }

                @Override
                public void onFailure(Throwable e) {
                    Timber.e("calculateDirections: Failed to get directions: %s", e.getMessage() );
                }
            });
    }

    private void saveUserLocation(Location location, String durationText, long timeLeft) {
        try {
            co.chatsdk.core.dao.User currentUser = ChatSDK.currentUser();
            if (currentUser != null) {
                User user = new User(currentUser.getEntityID(), currentUser.getName(), currentUser.getAvatarURL());
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                UserLocation userLocation = new UserLocation(user, geoPoint, null, durationText, timeLeft);

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.users_location_collection))
                        .document(user.getId());

                userLocationRef.set(userLocation)
                        .addOnSuccessListener(aVoid -> {
                            Timber.tag(TAG).d("DocumentSnapshot successfully written!");
                            Timber.d("onComplete: \ninserted user location into database." +
                                    "\n latitude: " + userLocation.getGeoPoint().getLatitude() +
                                    "\n longitude: " + userLocation.getGeoPoint().getLongitude());
                        })
                        .addOnFailureListener(e -> {
                            Timber.d("saveUser: Failed to write new user to firestore: %s", e.getMessage());
                        });
            }
        } catch (NullPointerException e) {
            Timber.e("saveUserLocation: stopping location service = %s", e.getMessage());
            stopSelf();
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if ("com.zacle.scheduler.service.location.LocationUpdatesService".equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
