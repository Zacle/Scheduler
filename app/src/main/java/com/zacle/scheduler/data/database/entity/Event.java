package com.zacle.scheduler.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.zacle.scheduler.utils.EventStatus;

import java.util.Date;

/**
 * Event entity to be saved to the database
 */

@Entity(tableName = "events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private Date time;
    private double destinationLat;
    private double destinationLong;
    private int notification_time;
    private String notification_settings;
    private boolean isCompleted;
    private EventStatus status;

    public Event() {}

    @Ignore
    public Event(String name, Date time, double destinationLat,
                 double destinationLong, int notification_time, String notification_settings,
                 boolean isCompleted, EventStatus status) {
        this.name = name;
        this.time = time;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.notification_time = notification_time;
        this.notification_settings = notification_settings;
        this.isCompleted = isCompleted;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(double destinationLong) {
        this.destinationLong = destinationLong;
    }

    public int getNotification_time() {
        return notification_time;
    }

    public void setNotification_time(int notification_time) {
        this.notification_time = notification_time;
    }

    public String getNotification_settings() {
        return notification_settings;
    }

    public void setNotification_settings(String notification_settings) {
        this.notification_settings = notification_settings;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}
