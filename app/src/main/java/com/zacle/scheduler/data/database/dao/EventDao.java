package com.zacle.scheduler.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.zacle.scheduler.data.database.entity.EventEntity;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for individual events to query data saved in the database
 */

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventEntity event);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);

    @Query("DELETE FROM events")
    void deleteAll();

    @Query("SELECT * FROM events WHERE time >= :date AND isCompleted = 0")
    LiveData<List<EventEntity>> getFutureEvents(Date date);

    @Query("SELECT * FROM events WHERE time < :date OR isCompleted = 1")
    LiveData<List<EventEntity>> getPastEvents(Date date);

    @Query("SELECT * FROM events WHERE id = :eventId")
    LiveData<EventEntity> getEvent(int eventId);
}
