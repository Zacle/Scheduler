package com.zacle.scheduler.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.zacle.scheduler.utils.AppConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EventAlarm implements Alarm {

    private static final String TAG = "EventAlarm";
    public static final String EVENT_NAME = "com.zacle.scheduler.service.alarm.name";
    public static final String EVENT_ID = "com.zacle.scheduler.service.alarm.id";

    private Context context;
    private Calendar calendar;
    private Intent intent;
    private long date;

    public EventAlarm(Context context, long date, int time, String settings) {
        this.context = context;
        this.date = date;
        intent = new Intent(context, EventAlarmReceiver.class);
        calendar = Calendar.getInstance();
        setTime(time, settings);
    }

    @Override
    public void startAlarm(int id, String name) {
        intent.putExtra(EVENT_NAME, name);
        intent.putExtra(EVENT_ID, id);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void editAlarm(int id, String name) {
        intent.putExtra(EVENT_NAME, name);
        intent.putExtra(EVENT_ID, id);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void cancelAlarm(int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    private void setTime(int time, String settings) {
        Log.d(TAG, "setTime: before date = " + date + ", actual date = " + new Date(date).toString());
        if (settings.equals(AppConstants.MINUTES)) {
            Log.d(TAG, "setTime: " + settings + ", milliseconds = " + TimeUnit.MINUTES.toMillis(time));
            date = date - TimeUnit.MINUTES.toMillis(time);
        } else {
            Log.d(TAG, "setTime: " + settings + ", milliseconds = " + TimeUnit.HOURS.toMillis(time));
            date = date - TimeUnit.HOURS.toMillis(time);
        }
        calendar.setTimeInMillis(date);
        Log.d(TAG, "setTime: calendar date = " + calendar.getTime().toString());
    }
}
