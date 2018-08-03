package com.chunchiehliang.androidarchitecureexample.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author Chun-Chieh Liang on 8/1/18.
 */

@Entity(tableName = "book")
public class Book {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "book_title")
    private String title;

    @ColumnInfo(name = "book_author")
    private String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
