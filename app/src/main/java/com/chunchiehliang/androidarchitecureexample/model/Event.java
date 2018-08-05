package com.chunchiehliang.androidarchitecureexample.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * @author Chun-Chieh Liang on 8/1/18.
 */

@Entity(tableName = "event")
public class Event {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "event_title")
    private String title;

    @ColumnInfo(name = "event_description")
    private String description;

    @ColumnInfo(name = "event_date")
    private Date date;

    @ColumnInfo(name = "event_bookmark")
    private boolean isBookmarked;

    @Ignore
    public Event(String title, String description, Date date, boolean isBookmarked) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.isBookmarked = isBookmarked;
    }

    public Event(int id, String title, String description, Date date, boolean isBookmarked) {
        this(title, description, date, isBookmarked);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
}
