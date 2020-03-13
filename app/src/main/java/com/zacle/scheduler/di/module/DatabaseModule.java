package com.zacle.scheduler.di.module;

import android.app.Application;

import androidx.room.Room;

import com.zacle.scheduler.data.database.SchedulerDatabase;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.di.annotation.DatabaseInfo;
import com.zacle.scheduler.utils.AppConstants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Database module for dependency injection
 */

@Module
public class DatabaseModule {

    @Provides
    @DatabaseInfo
    static String provideDatabaseName() {
        return AppConstants.DB_NAME;
    }

    @Singleton
    @Provides
    static SchedulerDatabase provideDatabase(Application application, @DatabaseInfo String dbName) {
        return Room.databaseBuilder(application, SchedulerDatabase.class, dbName)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    static EventDao provideEventDao(SchedulerDatabase db) {
        return db.eventDao();
    }
}
