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
    private String source;
    private String destination;
    private boolean isCompleted;
    private EventStatus status;

    public Event() {}

    @Ignore
    public Event(String name, Date time, String source, String destination, boolean isCompleted, EventStatus status) {
        this.name = name;
        this.time = time;
        this.source = source;
        this.destination = destination;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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
