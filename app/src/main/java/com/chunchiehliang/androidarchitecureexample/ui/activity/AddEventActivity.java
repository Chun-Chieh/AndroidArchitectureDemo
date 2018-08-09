package com.chunchiehliang.androidarchitecureexample.ui.activity;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;
import com.chunchiehliang.androidarchitecureexample.viewmodel.EventViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static com.chunchiehliang.androidarchitecureexample.Utils.dateToString;
import static com.chunchiehliang.androidarchitecureexample.Utils.stringToDate;
import static java.lang.Integer.parseInt;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = AddEventActivity.class.getSimpleName();

    // Extra for the event ID to be received in the intent
    public static final String EXTRA_EVENT_ID = "extraEventId";
    // Extra for the event ID to be received after rotation
    public static final String INSTANCE_EVENT_ID = "instanceEventId";
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_EVENT_ID = -1;

    private int mEventId = DEFAULT_EVENT_ID;

    private Button mBtnAddEvent;
    private EditText mEditTextTitle, mEditTextDescription, mEditTextDate;
    private ImageView mImageDate;
    private TextInputLayout mTextInputLayoutTitle, mTextInputLayoutDescription, mTextInputLayoutDate;
    private CheckBox mCheckBoxBookmark;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_EVENT_ID)) {
            mEventId = savedInstanceState.getInt(INSTANCE_EVENT_ID, DEFAULT_EVENT_ID);
        }

        initDb();
        initView();
    }

    /**
     * Save the event id for configuration changes
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_EVENT_ID, mEventId);
        super.onSaveInstanceState(outState);
    }

    private void initDb() {
        mDb = AppDatabase.getInstance(getApplicationContext());
    }

    private void initView() {
        mBtnAddEvent = findViewById(R.id.btn_add_event);
        mEditTextTitle = findViewById(R.id.et_title);
        mEditTextDescription = findViewById(R.id.et_description);
        mEditTextDate = findViewById(R.id.et_date);
        mImageDate = findViewById(R.id.image_date);
        mTextInputLayoutTitle = findViewById(R.id.text_input_layout_title);
        mTextInputLayoutDescription = findViewById(R.id.text_input_layout_description);
        mTextInputLayoutDate = findViewById(R.id.text_input_layout_date);
        mCheckBoxBookmark = findViewById(R.id.checkbox_bookmark);

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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_EVENT_ID)) {
            mBtnAddEvent.setText(R.string.btn_update_event);

            mEventId = intent.getIntExtra(EXTRA_EVENT_ID, DEFAULT_EVENT_ID);

            EventViewModel.EventViewModelFactory factory = new EventViewModel.EventViewModelFactory(mDb, mEventId);

            final EventViewModel viewModel = ViewModelProviders.of(this, factory).get(EventViewModel.class);

            viewModel.getEvent().observe(this, new Observer<Event>() {
                @Override
                public void onChanged(@Nullable Event eventEntry) {
                    viewModel.getEvent().removeObserver(this);
                    if (eventEntry != null) {
                        mEditTextTitle.setText(eventEntry.getTitle());
                        mEditTextDescription.setText(eventEntry.getDescription());
                        mEditTextDate.setText(dateToString(eventEntry.getDate()));
                        mCheckBoxBookmark.setChecked(eventEntry.isBookmarked());
                    }
                }
            });
        }

    }

    private void onSaveButtonClicked() {

        String title = mEditTextTitle.getText().toString();
        title = title.equals("") ? "Untitled" : title;
        String description = mEditTextDescription.getText().toString();
        String dateString = mEditTextDate.getText().toString();
        Date date = isValidDate(dateString) ? stringToDate(dateString) : new Date();
        boolean isBookmarked = mCheckBoxBookmark.isChecked();

        if (isValidInput()) {
            final Event event = new Event(title, description, date, isBookmarked);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
//                    Log.d(TAG, String.format("new event id: %d; exist Id: %d", event.getId(), mEventId));
                    if (mEventId == DEFAULT_EVENT_ID) {
                        // insert new task
                        mDb.eventDao().insertEvent(event);
                    } else {
                        //update task
                        event.setId(mEventId);
                        mDb.eventDao().updateEvent(event);
                    }
                    finish();
                }
            });
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

                mEditTextDate.setText(dateToString(currentCalendar.getTime()));
            }
        };

        DatePickerDialog datePickerDialog;
        String dateString = mEditTextDate.getText().toString();

        if (!dateString.equals("")) {
            Date inputDate = stringToDate(dateString);
            Calendar presetCalendar = Calendar.getInstance();

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


    private boolean isValidInput() {
        boolean isValid;
        Date date = stringToDate(mEditTextDate.getText().toString());

        if (date == null || date.toString().equals("")) {
            mTextInputLayoutDate.setError(getString(R.string.input_error_message_no_input));
            isValid = false;
        } else if (!isValidDate(mEditTextDate.getText().toString())) {
            mTextInputLayoutDate.setError(getString(R.string.input_error_message_invalid_date));
            isValid = false;
        } else {
            mTextInputLayoutDate.setErrorEnabled(false);
            isValid = true;
        }
        return isValid;
    }


    private boolean isValidDate(String dateString) {

//        Log.d(TAG, "Date: " + dateString);
        // First check for the pattern
        if (!Pattern.matches(("\\d\\d/\\d\\d/\\d\\d\\d\\d"), dateString))
            return false;

        // Parse the date parts to integers
        String[] parts = dateString.split("/");
        int month = parseInt(parts[0]);
        int day = parseInt(parts[1]);
        int year = parseInt(parts[2]);

        // Check the ranges of month and year
        if (year < 1900 || year > 3000 || month == 0 || month > 12)
            return false;

        int[] monthLength = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // Adjust for leap years
        if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
            monthLength[1] = 29;

        // Check the range of the day
        return day > 0 && day <= monthLength[month - 1];
    }
}
