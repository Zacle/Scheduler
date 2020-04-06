package com.zacle.scheduler.di.module;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.zacle.scheduler.data.database.SchedulerDatabase;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.data.database.entity.Event;
import com.zacle.scheduler.di.annotation.DatabaseInfo;
import com.zacle.scheduler.utils.AppConstants;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.zacle.scheduler.utils.EventStatus.COMING;

/**
 * Database module for dependency injection
 */

@Module
public class DatabaseModule {

    private static SchedulerDatabase instance;

    @Provides
    @DatabaseInfo
    static String provideDatabaseName() {
        return AppConstants.DB_NAME;
    }

    @Singleton
    @Provides
    static SchedulerDatabase provideDatabase(Application application, @DatabaseInfo String dbName) {
        instance = Room.databaseBuilder(application, SchedulerDatabase.class, dbName)
                .fallbackToDestructiveMigration()
                .addCallback(roomCallback)
                .build();
        return instance;
    }

    @Singleton
    @Provides
    static EventDao provideEventDao(SchedulerDatabase db) {
        return db.eventDao();
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private EventDao eventDao;

        private PopulateDbAsyncTask(SchedulerDatabase db) {
            eventDao = db.eventDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            eventDao.insert(new Event("First Event", getDate(2020, 3, 21, 12, 30), 13.90376, 42.90605, -14.39926, -14.39926, 10, AppConstants.MINUTES, false, COMING));
            eventDao.insert(new Event("Second Event", getDate(2020, 3, 22, 12, 30), 34.34270, 110.96384, -3.92924, 25.36992, 30, AppConstants.MINUTES, false, COMING));
            eventDao.insert(new Event("Third Event", getDate(2020, 3, 23, 12, 30), 61.44640, 160.23139, 22.52265, 29.83466, 1, AppConstants.HOURS, false, COMING));
            eventDao.insert(new Event("Fourth Event", getDate(2020, 3, 24, 12, 30), -23.06563, 47.31244, 68.92495, -143.81223, 2, AppConstants.HOURS, false, COMING));
            eventDao.insert(new Event("Fifth Event", getDate(2020, 3, 25, 12, 30), 27.31039, 107.75355, -15.26570, 131.02039, 30, AppConstants.MINUTES, false, COMING));
            return null;
        }

        private Date getDate(int year, int month, int day, int hour, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);
            return calendar.getTime();
        }
    }
}
