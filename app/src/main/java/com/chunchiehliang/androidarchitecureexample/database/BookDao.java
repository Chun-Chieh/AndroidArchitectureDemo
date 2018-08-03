package com.chunchiehliang.androidarchitecureexample.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.util.List;

/**
 * @author Chun-Chieh Liang on 8/2/18.
 */

@Dao
public interface BookDao {
    @Query("SELECT * FROM book ORDER BY book_publication_date")
    List<Book> loadAllBooks();

    @Insert
    void insertBook(Book book);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);

    @Query("SELECT * FROM book WHERE id = :id")
    Book loadBookById(int id);
}
