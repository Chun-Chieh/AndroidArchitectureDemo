package com.chunchiehliang.androidarchitecureexample.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.util.Date;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private Button mBtnAddBook;
    private EditText mEditTextTitle, mEditTextAuthor;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        initDb();
        initView();
    }


    private void initDb(){
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void initView() {
        mBtnAddBook = findViewById(R.id.btn_add_book);
        mEditTextTitle = findViewById(R.id.et_title);
        mEditTextAuthor = findViewById(R.id.et_author);

        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = mEditTextTitle.getText().toString();
                String author = mEditTextAuthor.getText().toString();

                mDb.bookDao().insertBook(new Book(title, author, new Date()));

                finish();
            }
        });
    }
}
