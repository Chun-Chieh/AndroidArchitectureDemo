package com.chunchiehliang.androidarchitecureexample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.util.List;

/**
 * @author Chun-Chieh Liang on 8/6/18.
 */
public class EventListViewModel extends AndroidViewModel{

    private static final String TAG = EventListViewModel.class.getSimpleName();

    private LiveData<List<Event>> events;

    public EventListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        events = database.eventDao().loadAllEvents();
    }


    public LiveData<List<Event>> getEvents() {
        return events;
    }
}
