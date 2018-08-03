package com.chunchiehliang.androidarchitecureexample.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private Button mBtnAddBook;
    private EditText mEditTextTitle, mEditTextAuthor, mEditTextPubDate;
    private ImageView mImagePubDate;
    private TextInputLayout mTextInputLayoutPubDate;

    private AppDatabase mDb;


    private SimpleDateFormat mDateFormat;

    private boolean isValid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        initDb();
        initView();
    }

    private void initDb() {
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void initView() {
        setDateFormatter();

        mBtnAddBook = findViewById(R.id.btn_add_book);
        mEditTextTitle = findViewById(R.id.et_title);
        mEditTextAuthor = findViewById(R.id.et_author);
        mEditTextPubDate = findViewById(R.id.et_pub_date);
        mImagePubDate = findViewById(R.id.image_pub_date);
        mTextInputLayoutPubDate = findViewById(R.id.text_input_layout_pub_date);

        mImagePubDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDateImageClicked();
            }
        });

        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });


    }

    private void onSaveButtonClicked() {

        String title = mEditTextTitle.getText().toString();
        String author = mEditTextAuthor.getText().toString();

        // set to today if there's no input
        Date date = new Date();
        try {
            mDateFormat.setLenient(false);
            date = mDateFormat.parse(mEditTextPubDate.getText().toString());
        } catch (ParseException e) {
//            e.printStackTrace();
            isValid = false;
            mTextInputLayoutPubDate.setError("Invalid Date!");
        }

        if (isValid) {
            final Book book = new Book(title, author, date);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.bookDao().insertBook(book);
                }
            });
            finish();
        }
    }

    private void onDateImageClicked() {
        // current time
        final Calendar currentCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                currentCalendar.set(Calendar.YEAR, year);
                currentCalendar.set(Calendar.MONTH, monthOfYear);
                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mEditTextPubDate.setText(mDateFormat.format(currentCalendar.getTime()));
            }
        };

        DatePickerDialog datePickerDialog;

        String dateString = mEditTextPubDate.getText().toString();

        if (!dateString.equals("")) {
            Date inputDate = null;
            Calendar presetCalendar = Calendar.getInstance();
            try {
                inputDate = mDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            presetCalendar.setTime(inputDate);
            datePickerDialog = new DatePickerDialog(AddBookActivity.this, onDateSetListener,
                    presetCalendar.get(Calendar.YEAR),
                    presetCalendar.get(Calendar.MONTH),
                    presetCalendar.get(Calendar.DAY_OF_MONTH));
        } else {
            datePickerDialog = new DatePickerDialog(AddBookActivity.this, onDateSetListener,
                    currentCalendar.get(Calendar.YEAR),
                    currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH));
        }
        datePickerDialog.show();
    }


    private void setDateFormatter() {
        String DATE_FORMAT = "MM/dd/yyyy";
        mDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    }
}
