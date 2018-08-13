package com.chunchiehliang.androidarchitecureexample.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.chunchiehliang.androidarchitecureexample.BaseApp;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.util.List;

/**
 * @author Chun-Chieh Liang on 8/6/18.
 */
public class EventListViewModel extends ViewModel {

    private static final String TAG = EventListViewModel.class.getSimpleName();

    public static final int FILTER_ALL = 0;
    public static final int FILTER_BOOKMARK = 1;

    private LiveData<List<Event>> events;

    public EventListViewModel() {
        events = getEventsWithFilter(FILTER_ALL);
    }


    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public LiveData<List<Event>> getEventsWithFilter(int filter) {
        AppDatabase database = AppDatabase.getInstance(BaseApp.getContext());
        switch (filter) {
            case FILTER_ALL:
                return database.eventDao().loadAllEvents();

            case FILTER_BOOKMARK:
                return database.eventDao().loadEventsWithBookmark();

            default:
                return database.eventDao().loadAllEvents();
        }
    }

    public void replaceSubscription(LifecycleOwner lifecycleOwner, int filter) {
        events.removeObservers(lifecycleOwner);
        events = getEventsWithFilter(filter);
    }
}
