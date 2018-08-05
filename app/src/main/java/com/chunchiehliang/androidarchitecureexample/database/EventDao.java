package com.chunchiehliang.androidarchitecureexample.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.util.List;

/**
 * @author Chun-Chieh Liang on 8/2/18.
 */

@Dao
public interface EventDao {
    @Query("SELECT * FROM Event ORDER BY event_date")
    List<Event> loadAllEvents();

    @Insert
    void insertEvent(Event event);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEvent(Event event);

    @Delete
    void deleteEvent(Event event);

    @Query("SELECT * FROM Event WHERE id = :id")
    Event loadEventById(int id);
}
