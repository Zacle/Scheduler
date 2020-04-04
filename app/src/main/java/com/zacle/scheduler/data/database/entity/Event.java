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
    private double sourceLat;
    private double sourceLong;
    private double destinationLat;
    private double destinationLong;
    private boolean isCompleted;
    private EventStatus status;

    public Event() {}

    @Ignore
    public Event(String name, Date time, double sourceLat, double sourceLong,
                 double destinationLat, double destinationLong, boolean isCompleted, EventStatus status) {
        this.name = name;
        this.time = time;
        this.sourceLat = sourceLat;
        this.sourceLong = sourceLong;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
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

    public double getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(double sourceLat) {
        this.sourceLat = sourceLat;
    }

    public double getSourceLong() {
        return sourceLong;
    }

    public void setSourceLong(double sourceLong) {
        this.sourceLong = sourceLong;
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
