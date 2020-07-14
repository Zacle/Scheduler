package com.zacle.scheduler.ui.addOrEdit;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import com.zacle.scheduler.R;
import com.zacle.scheduler.utils.AppConstants;
import com.zacle.scheduler.utils.DateUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class AddEditEvent {

    private static final String TAG = "AddEditEvent";

    private final int INIT = -1;

    private int id = INIT;
    private String name = "";
    private long date = INIT;
    private double destinationLat = INIT;
    private double destinationLong = INIT;
    private int status = 0;
    private int newHour = 0;
    private int newMinute = 0;
    private int newYear = INIT;
    private int newMonth = INIT;
    private int newDay = INIT;
    private int notification_time = 30;
    private String notification_settings = "minutes";
    private boolean destinationModified = false;


    private final Activity activity;

    public AddEditEvent(Activity activity) {
        this.activity = activity;
    }

    public AddEditEvent(Activity activity, int id, String name, long date,
                        double destinationLat, double destinationLong, int notification_time,
                        String notification_settings, int status) {
        this.activity = activity;
        this.id = id;
        this.name = name;
        this.date = date;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.notification_time = notification_time;
        this.notification_settings = notification_settings;
        this.status = status;
        init();
    }

    private void init() {
        setDate();
        setTime();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public int getStatus() {
        return status;
    }

    public int getNotification_time() {
        return notification_time;
    }

    public String getNotification_settings() {
        return notification_settings;
    }

    public void setNotification_time(int notification_time) {
        this.notification_time = notification_time;
    }

    public void setNotification_settings(String notification_settings) {
        this.notification_settings = notification_settings;
    }

    private void setDate() {
        Date formated = new Date(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formated);
        newYear = calendar.get(Calendar.YEAR);
        newMonth = calendar.get(Calendar.MONTH);
        newDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void setTime() {
        Date formated = new Date(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formated);
        newHour = calendar.get(Calendar.HOUR_OF_DAY);
        newMinute = calendar.get(Calendar.MINUTE);
    }

    public String getDestinationLocation() {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        String location = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(destinationLat, destinationLong, 1);
            Address address = addresses.get(0);
            location = address.getAddressLine(0);
        } catch(IOException e) {
            Timber.e("Geocoder I/O Exception %s", e.getMessage());
        }
        return location;
    }

    private String getTimePlural(String time) {
        if (time.equals(AppConstants.MINUTES)) {
            return (notification_time == 1) ? activity.getString(R.string.minute) : activity.getString(R.string.minutes);
        }
        return (notification_time == 1) ? activity.getString(R.string.hour) : activity.getString(R.string.hours);
    }

    public String getNotificationText() {
        return notification_time + " " + getTimePlural(notification_settings) + " " + activity.getString(R.string.before);
    }

    public void setDate(int year, int month, int day) {
        newYear = year;
        newMonth = month;
        newDay = day;
    }

    public void setTime(int hour, int minute) {
        newHour = hour;
        newMinute = minute;
    }

    public void setDestination(double lat, double lng, boolean modified) {
        destinationLat = lat;
        destinationLong = lng;
        destinationModified = modified;
    }

    public boolean checkAndSaveName(String name) {
        this.name = name;
        return !name.equals("");
    }

    public boolean checkAndSaveDate() {
        if (id != INIT && newYear == INIT) {
            return false;
        }
        long validate = DateUtil.getDate(newYear, newMonth, newDay, newHour, newMinute).getTime();
        long currentTime = new Date().getTime();
        if (validate < currentTime) {
            return false;
        }
        date = validate;
        return true;
    }

    public boolean checkDestination() {
        if (id != INIT) {
            return true;
        }
        return destinationModified;
    }

    public boolean isNewEvent() {
        return id == INIT;
    }
}