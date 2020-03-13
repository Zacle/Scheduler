package com.zacle.scheduler.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.zacle.scheduler.data.database.model.Event;

import java.util.Date;

/**
 * Event entity to be saved to the database
 */

@Entity(tableName = "events")
public class EventEntity implements Event {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date time;
    private String source;
    private String destination;
    private boolean isCompleted;

    public EventEntity() {}

    @Ignore
    public EventEntity(int id, Date time, String source, String destination, boolean isCompleted) {
        this.id = id;
        this.time = time;
        this.source = source;
        this.destination = destination;
        this.isCompleted = isCompleted;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}