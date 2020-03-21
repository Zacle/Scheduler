package com.zacle.scheduler.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.zacle.scheduler.data.database.SchedulerDatabase;
import com.zacle.scheduler.data.database.dao.EventDao;
import com.zacle.scheduler.data.database.entity.Event;
import com.zacle.scheduler.utils.AppExecutors;
import com.zacle.scheduler.utils.Resource;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/*
     Repository that handles Event instances
 */
@Singleton
public class EventRepository {
    private static final String TAG = "EventRepository";

    private final SchedulerDatabase database;
    private final EventDao eventDao;
    private final AppExecutors appExecutors;

    @Inject
    public EventRepository(AppExecutors appExecutors, SchedulerDatabase database, EventDao eventDao) {
        this.appExecutors = appExecutors;
        this.database = database;
        this.eventDao = eventDao;
    }

    public LiveData<Resource<List<Event>>> loadEvents() {
        return new NetworkBoundResource<List<Event>, List<Event>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<Event> item) {
                eventDao.insertEvents(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Event> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<List<Event>> loadFromDb() {
                Log.d(TAG, "loadFromDb: date = " + (new Date()).toString());
                return eventDao.getFutureEvents();
            }
        }.asLiveData();
    }

    public LiveData<Resource<Event>> insertEvent(Event event) {
        return new NetworkBoundResource<Event, Event>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Event item) {
                eventDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Event data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Event> loadFromDb() {
                return eventDao.getEvent(event.getId());
            }
        }.asLiveData();
    }

    public LiveData<Resource<Event>> updateEvent(Event event) {
        return new NetworkBoundResource<Event, Event>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Event item) {
                eventDao.update(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Event data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Event> loadFromDb() {
                return eventDao.getEvent(event.getId());
            }
        }.asLiveData();
    }

    public LiveData<Resource<Event>> deleteEvent(Event event) {
        return new NetworkBoundResource<Event, Event>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Event item) {
                eventDao.delete(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Event data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Event> loadFromDb() {
                LiveData<Event> deleted =  eventDao.getEvent(event.getId());
                eventDao.delete(event);
                return deleted;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Event>>> deleteAllEvents() {
        return new NetworkBoundResource<List<Event>, List<Event>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<Event> item) {
                eventDao.deleteAll();
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Event> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<List<Event>> loadFromDb() {
                LiveData<List<Event>> deleted = eventDao.getFutureEvents();
                eventDao.deleteAll();
                return deleted;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Event>>> loadPastEvents() {
        return new NetworkBoundResource<List<Event>, List<Event>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<Event> item) {
                eventDao.insertEvents(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Event> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<List<Event>> loadFromDb() {
                return eventDao.getPastEvents();
            }
        }.asLiveData();
    }
}
