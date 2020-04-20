package com.zacle.scheduler.service.geofence;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.zacle.scheduler.ui.map.RunningEventActivity;
import com.zacle.scheduler.utils.GeofenceErrorMessages;


public class GeofencingIntentService extends IntentService implements OnCompleteListener {

    private static final String TAG = "GeofencingIntentService";
    
    public static final String STOP_GEOFENCING = "com.zacle.scheduler.service.geofence.stop_geofencing";
    public static final String IGNORE_GEOFENCING = "com.zacle.scheduler.service.geofence.ignore_geofencing";


    private GeofencingClient geofencingClient;

    public GeofencingIntentService() {
        super("GeofencingIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        execute(intent, intent.getAction());
    }

    private void execute(Intent intent, String action) {
        if (action.equals(STOP_GEOFENCING)) {
            setStopGeofencing(intent);
        } else if (action.equals(IGNORE_GEOFENCING)) {

        }
    }

    private void setStopGeofencing(Intent intent) {
        geofencingClient = LocationServices.getGeofencingClient(this);
        
        int id = intent.getIntExtra(RunningEventActivity.ID, -1);
        
        geofencingClient.removeGeofences(getGeofencePendingIntent(id))
                .addOnCompleteListener(this);
    }

    /**
     * Gets a PendingIntent to send with the request to remove Geofence. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent(int id) {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()) {
            Log.d(TAG, "onComplete: Geofence Removed");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }
}
