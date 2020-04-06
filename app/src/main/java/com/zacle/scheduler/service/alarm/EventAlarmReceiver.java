package com.zacle.scheduler.service.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.zacle.scheduler.utils.NotificationHelper;

public class EventAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = 1;
        String name = "event";
        if (intent.hasExtra(EventAlarm.EVENT_ID)) {
            id = intent.getIntExtra(EventAlarm.EVENT_ID, 1);
            name = intent.getStringExtra(EventAlarm.EVENT_NAME);
        }
        NotificationHelper notificationHelper = new NotificationHelper(context, name, name);
        NotificationCompat.Builder builder = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(id, builder.build());
    }
}
