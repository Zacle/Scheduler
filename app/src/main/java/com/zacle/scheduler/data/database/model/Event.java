package com.zacle.scheduler.data.database.model;


import java.util.Date;

/**
 * Event interface to define EventEntity
 */

public interface Event {
    int getId();
    Date getTime();
    String getSource();
    String getDestination();
    boolean isCompleted();
}
