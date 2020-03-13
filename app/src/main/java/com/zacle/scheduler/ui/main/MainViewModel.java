package com.zacle.scheduler.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.zacle.scheduler.data.database.model.Event;
import com.zacle.scheduler.repository.EventRepository;
import com.zacle.scheduler.utils.Resource;

import java.util.List;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private final LiveData<Resource<List<Event>>> events;
    private final EventRepository eventRepository;

    @Inject
    public MainViewModel(EventRepository eventRepository) {
        Log.d(TAG, "MainViewModel: mainviewmodel is ready...");
        this.eventRepository = eventRepository;
        this.events = eventRepository.loadEvents();
    }

    public void insert(Event event) {
        eventRepository.insertEvent(event);
    }

    public void delete(Event event) {
        eventRepository.deleteEvent(event);
    }

    public void update(Event event) {
        eventRepository.updateEvent(event);
    }

    public LiveData<Resource<List<Event>>> getFutureEvents() {
        return eventRepository.loadEvents();
    }

    public LiveData<Resource<List<Event>>> getPastEvents() {
        return eventRepository.loadPastEvents();
    }

    public void deleteAllEvents() {
        eventRepository.deleteAllEvents();
    }
}
