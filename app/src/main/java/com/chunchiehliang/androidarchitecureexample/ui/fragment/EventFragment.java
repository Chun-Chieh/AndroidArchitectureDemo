package com.chunchiehliang.androidarchitecureexample.ui.fragment;


import android.app.ActionBar;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chunchiehliang.androidarchitecureexample.BaseApp;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;
import com.chunchiehliang.androidarchitecureexample.ui.activity.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.chunchiehliang.androidarchitecureexample.Utils.getDayColor;
import static com.chunchiehliang.androidarchitecureexample.Utils.getDayDiff;

public class EventFragment extends Fragment {

    private static final String KEY_EVENT_ID = "event_id";


    private TextView mTextViewTitle, mTextViewDescription, mTextViewRemain, mTextViewDate;
    private ActionBar activityActionBar;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance(int eventId) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_event, container, false);

        retrieveEvent(getArguments().getInt(KEY_EVENT_ID, -1));
        mTextViewTitle = root.findViewById(R.id.tv_event_title);
        mTextViewDescription = root.findViewById(R.id.tv_event_description);
        mTextViewRemain = root.findViewById(R.id.tv_event_remain_day);
        mTextViewDate = root.findViewById(R.id.tv_event_date);


        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_event:
                Toast.makeText(getContext(), "Edit", Toast.LENGTH_SHORT).show();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }



    private void retrieveEvent(int id) {
        if (id == -1) {
            // no data
            mTextViewRemain.setText(getString(R.string.no_data_found));
        } else {
            final LiveData<Event> event = AppDatabase.getInstance(getActivity()).eventDao().loadEventById(id);
            event.observe(this, new Observer<Event>() {
                @Override
                public void onChanged(@Nullable Event eventEntry) {
                    event.removeObserver(this);
                    populateUI(eventEntry);
                }
            });
        }
    }

    private void populateUI(Event eventEntry) {
        if (eventEntry == null) {
            return;
        }

        String DATE_FORMAT = "MM/dd/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        mTextViewTitle.setText(eventEntry.getTitle());
        mTextViewDescription.setText(eventEntry.getDescription());
        mTextViewRemain.setText(String.valueOf(getDayDiff(eventEntry.getDate())));

        GradientDrawable drawable = (GradientDrawable) mTextViewRemain.getBackground();
        drawable.setTint(getDayColor(getDayDiff(eventEntry.getDate())));
        mTextViewDate.setText(dateFormat.format(eventEntry.getDate()));

        // set the action bar title
        ((MainActivity) getActivity()).setupActionBar(eventEntry.getTitle());
    }
}
