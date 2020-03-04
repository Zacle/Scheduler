package com.zacle.scheduler.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.zacle.scheduler.data.database.converter.DateConverter;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.data.database.entity.EventEntity;

/**
 * Main database of the app
 */

@Database(entities = {EventEntity.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class SchedulerDatabase extends RoomDatabase {

    public abstract EventDao eventDao();
}
