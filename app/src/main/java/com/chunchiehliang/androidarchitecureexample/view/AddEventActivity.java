package com.chunchiehliang.androidarchitecureexample.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = AddEventActivity.class.getSimpleName();

    private Button mBtnAddEvent;
    private EditText mEditTextTitle, mEditTextDescription, mEditTextDate;
    private ImageView mImageDate;
    private TextInputLayout mTextInputLayoutTitle, mTextInputLayoutDescription, mTextInputLayoutDate;

    private AppDatabase mDb;


    private SimpleDateFormat mDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initDb();
        initView();
    }

    private void initDb() {
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void initView() {
        setDateFormatter();

        mBtnAddEvent = findViewById(R.id.btn_add_event);
        mEditTextTitle = findViewById(R.id.et_title);
        mEditTextDescription = findViewById(R.id.et_description);
        mEditTextDate = findViewById(R.id.et_pub_date);
        mImageDate = findViewById(R.id.image_date);
        mTextInputLayoutTitle = findViewById(R.id.text_input_layout_title);
        mTextInputLayoutDescription = findViewById(R.id.text_input_layout_description);
        mTextInputLayoutDate = findViewById(R.id.text_input_layout_date);


        mImageDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDateImageClicked();
            }
        });

        mBtnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onSaveButtonClicked();
            }
        });
    }

    private void onSaveButtonClicked() {

        String title = mEditTextTitle.getText().toString();
        String description = mEditTextDescription.getText().toString();

        // set to today if there's no input
        Date date = null;
        try {
            date = mDateFormat.parse(mEditTextDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
//            mTextInputLayoutDate.setError("Invalid Date!");
        }

        if (isValidText(title, description, date)) {
            final Event event = new Event(title, description, date, false);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.eventDao().insertEvent(event);
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

                mEditTextDate.setText(mDateFormat.format(currentCalendar.getTime()));
            }
        };

        DatePickerDialog datePickerDialog;

        String dateString = mEditTextDate.getText().toString();

        if (!dateString.equals("")) {
            Date inputDate = null;
            Calendar presetCalendar = Calendar.getInstance();
            try {
                inputDate = mDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            presetCalendar.setTime(inputDate);
            datePickerDialog = new DatePickerDialog(AddEventActivity.this, onDateSetListener,
                    presetCalendar.get(Calendar.YEAR),
                    presetCalendar.get(Calendar.MONTH),
                    presetCalendar.get(Calendar.DAY_OF_MONTH));
        } else {
            datePickerDialog = new DatePickerDialog(AddEventActivity.this, onDateSetListener,
                    currentCalendar.get(Calendar.YEAR),
                    currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH));
        }
        datePickerDialog.show();

    }


    private boolean isValidText(String title, String description, Date date) {
        boolean isValid;

        if (date == null || date.toString().equals("")) {
            mTextInputLayoutDate.setError(getString(R.string.error_message_no_input));
            isValid = false;
        } else if (!isValidDate(mEditTextDate.getText().toString())) {
            mTextInputLayoutDate.setError(getString(R.string.error_message_invalid_date));
            isValid = false;
        } else {
            mTextInputLayoutDate.setErrorEnabled(false);
            isValid = true;
        }
        return isValid;
    }


    private boolean isValidDate(String dateString) {

        Log.d(TAG, "Date: " + dateString);
        // First check for the pattern
        if (!Pattern.matches(("\\d\\d/\\d\\d/\\d\\d\\d\\d"), dateString))
            return false;

        // Parse the date parts to integers
        String[] parts = dateString.split("/");
        int month = parseInt(parts[0]);
        int day = parseInt(parts[1]);
        int year = parseInt(parts[2]);

        // Check the ranges of month and year
        if (year < 1000 || year > 3000 || month == 0 || month > 12)
            return false;

        int[] monthLength = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // Adjust for leap years
        if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
            monthLength[1] = 29;

        // Check the range of the day
        return day > 0 && day <= monthLength[month - 1];
    }

    private void setDateFormatter() {
        String DATE_FORMAT = "MM/dd/yyyy";
        mDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    }
}
