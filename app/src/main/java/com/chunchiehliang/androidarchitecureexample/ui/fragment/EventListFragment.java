package com.chunchiehliang.androidarchitecureexample.ui.fragment;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;
import com.chunchiehliang.androidarchitecureexample.ui.EventAdapter;
import com.chunchiehliang.androidarchitecureexample.ui.activity.AddEventActivity;
import com.chunchiehliang.androidarchitecureexample.ui.activity.MainActivity;
import com.chunchiehliang.androidarchitecureexample.viewmodel.EventListViewModel;

import java.util.List;

import static android.view.View.GONE;

public class EventListFragment extends Fragment implements EventAdapter.ItemClickListener {

    public static final String TAG = EventListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    ProgressBar mProgressBar;

    private Context activityContext;
    private AppDatabase mDb;
    private EventAdapter mAdapter;

    public EventListFragment() {
        // Required empty public constructor
    }

    public static EventListFragment newInstance() {
        return new EventListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activityContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        initDb();
        iniViews(rootView);
        setupEventViewModel();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // set the action bar title
        ((MainActivity) getActivity()).setupActionBar(getString(R.string.app_name));
    }

    private void initDb() {
        mDb = AppDatabase.getInstance(activityContext);
    }


    private void iniViews(View rootView) {
        mProgressBar = rootView.findViewById(R.id.progressbar_event_list);
        mProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView = rootView.findViewById(R.id.recycler_view_event_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
        mRecyclerView.addItemDecoration(new MarginItemDecoration((int) (getResources().getDimension(R.dimen.md_16dp) / getResources().getDisplayMetrics().density)));

        mAdapter = new EventAdapter(activityContext, this);

        mRecyclerView.setAdapter(mAdapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                List<Event> eventList = mAdapter.getEventList();

                final Event removedEvent = eventList.remove(position);
                mAdapter.notifyItemRemoved(position);

                // Delete the item from the database
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.eventDao().deleteEvent(removedEvent);
                    }
                });


                Snackbar mSnackBar = Snackbar.make(rootView.findViewById(R.id.coordinator_event_list), getString(R.string.item_removed_string, removedEvent.getTitle()), Snackbar.LENGTH_LONG);
                mSnackBar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Add it back to the database
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.eventDao().insertEvent(removedEvent);
                            }
                        });
                    }
                });
                mSnackBar.show();
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                final ColorDrawable backgroundDelete = new ColorDrawable(activityContext.getColor(R.color.materialRed));
                backgroundDelete.setBounds((int) (viewHolder.itemView.getRight() + dX),
                        viewHolder.itemView.getTop(),
                        viewHolder.itemView.getRight(),
                        viewHolder.itemView.getBottom());
                backgroundDelete.draw(c);

//                final Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_launcher_foreground);
//                int itemHeight = viewHolder.itemView.getBottom() - viewHolder.itemView.getTop();
//                int iconTop = viewHolder.itemView.getTop() + (itemHeight - icon.getIntrinsicWidth()) / 2;
//                int iconMargin = (itemHeight - icon.getIntrinsicHeight()) / 2;
//                int iconLeft = viewHolder.itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
//                int iconRight = viewHolder.itemView.getRight() - iconMargin;
//                int iconBottom = iconTop + icon.getIntrinsicHeight();
//
//                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
//                icon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        }).attachToRecyclerView(mRecyclerView);

        mFab = rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityContext, AddEventActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupEventViewModel() {
        EventListViewModel eventListViewModel = ViewModelProviders.of(this).get(EventListViewModel.class);
        eventListViewModel.getEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> events) {
                // update UI
                mProgressBar.setVisibility(GONE);
                mAdapter.setEventList(events);

            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            ((MainActivity) getActivity()).showDetail(itemId);
        }

//        Toast.makeText(activityContext, "click listener, event ID:" + itemId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClickListener(Event event) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                if (event.isBookmarked()) {
                    event.setBookmarked(false);
                } else {
                    event.setBookmarked(true);
                }
                mDb.eventDao().updateEvent(event);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private class MarginItemDecoration extends RecyclerView.ItemDecoration {
        private int margin;

        MarginItemDecoration(int margin) {
            this.margin = margin;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = margin;
            }
            outRect.bottom = margin;
            outRect.left = margin;
            outRect.right = margin;
        }
    }
}
