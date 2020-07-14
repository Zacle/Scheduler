package com.zacle.scheduler.service.alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.main.MainActivity;
import com.zacle.scheduler.utils.NotificationHelper;

public class EventAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = 1;
        String channel = "event_alarm_channel";
        String name = "event";
        if (intent.hasExtra(EventAlarm.EVENT_ID)) {
            id = intent.getIntExtra(EventAlarm.EVENT_ID, 1);
            name = intent.getStringExtra(EventAlarm.EVENT_NAME);
            channel = intent.getStringExtra(EventAlarm.EVENT_NAME) + channel;
        }
        NotificationHelper notificationHelper = new NotificationHelper(context, name, channel);
        NotificationCompat.Builder builder = notificationHelper.getChannelNotification()
                .setContentText(context.getString(R.string.alarm_notification))
                .setContentTitle(name)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .addAction(goToEvent(context, id))
                .addAction(cancelEvent(context, id))
                .setAutoCancel(true);
        notificationHelper.getManager().notify(id, builder.build());
    }

    private NotificationCompat.Action goToEvent(Context context, int id) {
        Intent goToEventIntent = new Intent(context, MainActivity.class);
        goToEventIntent.putExtra(MainActivity.ID, id);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                id,
                goToEventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action(
                R.drawable.ic_alarm_on,
                context.getString(R.string.geofence_ok_button),
                pendingIntent
        );
    }

    private NotificationCompat.Action cancelEvent(Context context, int id) {
        Intent cancelEventIntent = AlarmRemainderIntentService.startActionCancel(context, id);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                -1,
                cancelEventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action(
                R.drawable.ic_alarm_off,
                context.getString(R.string.no),
                pendingIntent
        );
    }
}
