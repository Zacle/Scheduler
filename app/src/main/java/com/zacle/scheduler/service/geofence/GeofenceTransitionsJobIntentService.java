package com.zacle.scheduler.service.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.main.MainActivity;
import com.zacle.scheduler.ui.map.RunningEventActivity;
import com.zacle.scheduler.utils.GeofenceErrorMessages;
import com.zacle.scheduler.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsJobIntentService extends JobIntentService {

    private static final int JOB_ID = 573;

    private static final String TAG = "GeofenceTransitionsIS";

    private String CHANNEL_ID = "channel_01";

    private int id = 1;
    private String name = "geofence_channel";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleWork(Intent intent) {

        verifyIntent(intent);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification();
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private void verifyIntent(Intent intent) {
        if (intent.hasExtra(RunningEventActivity.ID)) {
            id = intent.getIntExtra(RunningEventActivity.ID, 1);
            CHANNEL_ID = intent.getStringExtra(RunningEventActivity.NAME) + name;
            name = intent.getStringExtra(RunningEventActivity.NAME);
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext(), name, CHANNEL_ID);
        NotificationCompat.Builder builder = notificationHelper.getChannelNotification()
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle(name)
                .setContentText(getString(R.string.geofence_notification_message))
                .setContentIntent(contentIntent())
                .addAction(stopGeofencing())
                .addAction(ignoreGeofencing())
                .setAutoCancel(true);
        notificationHelper.getManager().notify(id, builder.build());
    }

    private PendingIntent contentIntent() {
        Intent startActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        return PendingIntent.getActivity(
                getApplicationContext(),
                id,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Action stopGeofencing() {
        Intent stopGeofencingIntent = new Intent(getApplicationContext(), GeofencingIntentService.class);
        stopGeofencingIntent.putExtra(RunningEventActivity.ID, id);
        stopGeofencingIntent.setAction(GeofencingIntentService.STOP_GEOFENCING);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                id,
                stopGeofencingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_alarm_on,
                getString(R.string.geofence_ok_button),
                pendingIntent
        );
        return action;
    }

    private NotificationCompat.Action ignoreGeofencing() {
        Intent ignoreGeofencingIntent = new Intent(getApplicationContext(), GeofencingIntentService.class);
        ignoreGeofencingIntent.putExtra(RunningEventActivity.ID, id);
        ignoreGeofencingIntent.setAction(GeofencingIntentService.IGNORE_GEOFENCING);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                id,
                ignoreGeofencingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_alarm_on,
                getString(R.string.geofence_not_button),
                pendingIntent
        );
        return action;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
