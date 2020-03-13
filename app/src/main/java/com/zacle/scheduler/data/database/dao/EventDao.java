package com.zacle.scheduler.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.zacle.scheduler.data.database.entity.EventEntity;
import com.zacle.scheduler.data.database.model.Event;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for individual events to query data saved in the database
 */

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvents(List<Event> event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM events")
    void deleteAll();

    @Query("SELECT * FROM events WHERE time >= :date AND isCompleted = 0")
    LiveData<List<Event>> getFutureEvents(Date date);

    @Query("SELECT * FROM events WHERE time < :date OR isCompleted = 1")
    LiveData<List<Event>> getPastEvents(Date date);

    @Query("SELECT * FROM events WHERE id = :eventId")
    LiveData<Event> getEvent(int eventId);
}
