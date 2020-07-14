package com.zacle.scheduler.service.alarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class AlarmRemainderIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CANCEL = "com.zacle.scheduler.service.alarm.action.CANCEL";

    private static final String EXTRA_ID = "com.zacle.scheduler.service.alarm.action.ID";

    public AlarmRemainderIntentService() {
        super("AlarmRemainderIntentService");
    }

    /**
     * Starts this service to perform action Cancel with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static Intent startActionCancel(Context context, int id) {
        Intent intent = new Intent(context, AlarmRemainderIntentService.class);
        intent.setAction(ACTION_CANCEL);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CANCEL.equals(action)) {
                handleActionCancel(intent);
            }
        }
    }

    /**
     * Handle action Cancel in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCancel(Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_ID, -1);
        if (notificationId != -1) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.cancel(notificationId);
        }
    }
}
