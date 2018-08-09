package com.chunchiehliang.androidarchitecureexample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;

/**
 * @author Chun-Chieh Liang on 8/8/18.
 */
public class EventViewModel extends ViewModel{
    private LiveData<Event> event;


    public EventViewModel(AppDatabase database, int eventId) {
        event = database.eventDao().loadEventById(eventId);
    }

    public LiveData<Event> getEvent() {
        return event;
    }

    public static class EventViewModelFactory extends ViewModelProvider.NewInstanceFactory {

        private final AppDatabase mDb;
        private final int mEventId;


        public EventViewModelFactory(AppDatabase database, int eventId) {
            mDb = database;
            mEventId = eventId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new EventViewModel(mDb, mEventId);
        }
    }
}
