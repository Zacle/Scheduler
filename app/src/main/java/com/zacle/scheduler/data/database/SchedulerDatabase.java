package com.zacle.scheduler.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.zacle.scheduler.data.database.converter.DateConverter;
import com.zacle.scheduler.data.database.converter.StatusConverter;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.data.database.entity.Event;

/**
 * Main database of the app
 */

@Database(entities = {Event.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class, StatusConverter.class})
public abstract class SchedulerDatabase extends RoomDatabase {

    public abstract EventDao eventDao();
}
