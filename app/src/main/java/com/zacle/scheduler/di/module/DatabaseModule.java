package com.zacle.scheduler.di.module;

import android.content.Context;

import androidx.room.Room;

import com.zacle.scheduler.data.database.SchedulerDatabase;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.di.ApplicationContext;
import com.zacle.scheduler.di.DatabaseInfo;
import com.zacle.scheduler.utils.AppConstants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Database module for dependency injection
 */

@Module
public class DatabaseModule {
    @ApplicationContext
    private final Context mContext;

    public DatabaseModule(@ApplicationContext Context context) {
        mContext = context;
    }

    @Provides
    @DatabaseInfo
    String provideDatabaseName() {
        return AppConstants.DB_NAME;
    }

    @Singleton
    @Provides
    SchedulerDatabase provideDatabase(@DatabaseInfo String dbName) {
        return Room.databaseBuilder(mContext, SchedulerDatabase.class, dbName)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    EventDao provideEventDao(SchedulerDatabase db) {
        return db.eventDao();
    }
}
