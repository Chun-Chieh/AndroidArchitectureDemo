package com.chunchiehliang.androidarchitecureexample.ui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import static com.chunchiehliang.androidarchitecureexample.ui.activity.AddEventActivity.EXTRA_EVENT_ID;

public class EventListFragment extends Fragment implements EventAdapter.ItemClickListener {

    public static final String TAG = EventListFragment.class.getSimpleName();

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;

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
        mCoordinatorLayout = rootView.findViewById(R.id.coordinator_event_list);

        mProgressBar = rootView.findViewById(R.id.progressbar_event_list);
        mProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView = rootView.findViewById(R.id.recycler_view_event_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
        mRecyclerView.addItemDecoration(new MarginItemDecoration((int) (getResources().getDimension(R.dimen.dimen_16dp) / getResources().getDisplayMetrics().density)));

        // Removes blinks as magic
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        mAdapter = new EventAdapter(activityContext, this);

        mRecyclerView.setAdapter(mAdapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                List<Event> eventList = mAdapter.getEventList();
                deleteEvent(eventList.remove(position));
                mAdapter.notifyItemRemoved(position);
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
                // todo: test animation
//                final Slide transition = new Slide(Gravity.BOTTOM);
//                transition.setDuration(1200L);
//                TransitionManager.beginDelayedTransition((ViewGroup) mProgressBar.getRootView(), transition);
//
//                if(mProgressBar.getVisibility() == VISIBLE){
//                    mProgressBar.setVisibility(View.GONE);
//                } else {
//                    mProgressBar.setVisibility(View.VISIBLE);
//                }

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

    /**
     * This method has been overridden by onBindViewHolder
     *
     * @param eventId event id
     */
    @Override
    public void onItemClickListener(int eventId) {
//        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
//            ((MainActivity) getActivity()).showDetail(itemId);
//        }
//        Toast.makeText(activityContext, "event ID: " + eventId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClickListener(Event event) {
        createItemDialog(event);

    }

    private void createItemDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position of the selected item
                switch (which) {
                    case 0:
                        Intent intent = new Intent(activityContext, AddEventActivity.class);
                        intent.putExtra(EXTRA_EVENT_ID, event.getId());
                        startActivity(intent);
                        break;
                    case 1:
                        updateEventBookmark(event);
                        break;
                    case 2:
                        deleteEvent(event);
                        break;
                    default:
                        Toast.makeText(activityContext, "default", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
    }

    private void updateEventBookmark(Event event) {


        event.setBookmarked(!event.isBookmarked());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.eventDao().updateEvent(event);
            }
        });
    }

    private void deleteEvent(Event event) {
        final Event removedEvent = event;
        // Delete the event from the database
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.eventDao().deleteEvent(event);
            }
        });

        // recover the event
        Snackbar mSnackBar = Snackbar.make(mCoordinatorLayout, getString(R.string.msg_item_removed, removedEvent.getTitle()), BaseTransientBottomBar.LENGTH_LONG);
        mSnackBar.setAction(R.string.msg_undo, new View.OnClickListener() {
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
