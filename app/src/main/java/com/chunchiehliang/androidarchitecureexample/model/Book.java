package com.chunchiehliang.androidarchitecureexample.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

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

    @ColumnInfo(name = "book_publication_date")
    private Date publicationDate;

    @Ignore
    public Book(String title, String author, Date publicationDate) {
        this.title = title;
        this.author = author;
        this.publicationDate = publicationDate;
    }

    public Book(int id, String title, String author, Date publicationDate) {
        this(title, author, publicationDate);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }
}
