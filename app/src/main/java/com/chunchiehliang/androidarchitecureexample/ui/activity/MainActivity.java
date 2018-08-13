package com.chunchiehliang.androidarchitecureexample.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.ui.fragment.EventFragment;
import com.chunchiehliang.androidarchitecureexample.ui.fragment.EventListFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add event list fragment if this is first creation
        if (savedInstanceState == null) {
            EventListFragment fragment = EventListFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, EventListFragment.TAG).commit();
        }
    }

    /**
     * Shows the product detail fragment
     */
    public void showDetail(int eventId) {
        EventFragment eventFragment = EventFragment.newInstance(eventId);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("event")
                .replace(R.id.fragment_container,
                        eventFragment, null).commit();
    }
}
