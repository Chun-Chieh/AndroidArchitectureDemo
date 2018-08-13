package com.chunchiehliang.androidarchitecureexample.ui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.BaseApp;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;
import com.chunchiehliang.androidarchitecureexample.ui.EventAdapter;
import com.chunchiehliang.androidarchitecureexample.ui.activity.AddEventActivity;
import com.chunchiehliang.androidarchitecureexample.viewmodel.EventListViewModel;

import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.chunchiehliang.androidarchitecureexample.ui.activity.AddEventActivity.EXTRA_EVENT_ID;
import static com.chunchiehliang.androidarchitecureexample.viewmodel.EventListViewModel.FILTER_ALL;
import static com.chunchiehliang.androidarchitecureexample.viewmodel.EventListViewModel.FILTER_BOOKMARK;

public class EventListFragment extends Fragment implements EventAdapter.ItemClickListener {

    public static final String TAG = EventListFragment.class.getSimpleName();


    private boolean showAll = true;

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;

    private Context activityContext;
    private AppDatabase mDb;
    private EventAdapter mAdapter;

    private EventListViewModel eventListViewModel;


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

        setHasOptionsMenu(true);

        initDb();
        iniViews(rootView);
        setupEventListViewModel();
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_event_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_show_all:
                eventListViewModel.replaceSubscription(this, FILTER_ALL);
                setupEventListViewModel();
                showAll = true;
                return true;
            case R.id.action_show_bookmarked:
                eventListViewModel.replaceSubscription(this, FILTER_BOOKMARK);
                setupEventListViewModel();
                showAll = false;
                return true;
            case R.id.action_settings:
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_all).setVisible(!showAll);
        menu.findItem(R.id.action_show_bookmarked).setVisible(showAll);
        super.onPrepareOptionsMenu(menu);
    }

    private void initDb() {
        mDb = AppDatabase.getInstance(BaseApp.getContext());
    }


    private void iniViews(View rootView) {
        mCoordinatorLayout = rootView.findViewById(R.id.coordinator_event_list);

        mProgressBar = rootView.findViewById(R.id.progressbar_event_list);
        mProgressBar.setVisibility(VISIBLE);

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
            private boolean swipeBack = false;

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                List<Event> eventList = mAdapter.getEventList();

                if (direction == ItemTouchHelper.LEFT) {
                    deleteEvent(eventList.remove(position));
                    mAdapter.notifyItemRemoved(position);
                }

                if (direction == ItemTouchHelper.RIGHT) {
                    updateEventBookmark(eventList.get(position));
                    Toast.makeText(getContext(), "😀", Toast.LENGTH_SHORT).show();
                }
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

                float translationX = Math.min(-dX, viewHolder.itemView.getWidth() / 2);
                viewHolder.itemView.setTranslationX(-translationX);

                final ColorDrawable backgroundDelete = new ColorDrawable(activityContext.getColor(R.color.materialRed));
                backgroundDelete.setBounds(viewHolder.itemView.getRight() + (int) dX,
                        viewHolder.itemView.getTop(),
                        viewHolder.itemView.getRight(),
                        viewHolder.itemView.getBottom());
                backgroundDelete.draw(c);

                final Drawable icon = ContextCompat.getDrawable(activityContext, R.drawable.ic_delete);
                int itemHeight = viewHolder.itemView.getBottom() - viewHolder.itemView.getTop();
                int deleteIconTop = viewHolder.itemView.getTop() + (itemHeight - icon.getIntrinsicWidth()) / 2;
                int deleteIconMargin = (itemHeight - icon.getIntrinsicHeight()) / 3;
                int deleteIconLeft = viewHolder.itemView.getRight() - deleteIconMargin - icon.getIntrinsicWidth();
                int deleteIconRight = viewHolder.itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + icon.getIntrinsicHeight();

                icon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                icon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                return swipeBack ? 0 : super.convertToAbsoluteDirection(flags, layoutDirection);
            }
        }).attachToRecyclerView(mRecyclerView);

        // hide or reveal fab
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFab.getVisibility() == View.VISIBLE) {
                    mFab.hide();
                } else if (dy < 0 && mFab.getVisibility() != View.VISIBLE) {
                    mFab.show();
                }
            }
        });



        mFab = rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityContext, AddEventActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupEventListViewModel() {
        eventListViewModel = ViewModelProviders.of(this).get(EventListViewModel.class);
        eventListViewModel.getEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> events) {
                // update UI
                mAdapter.setEventList(events);
                mProgressBar.setVisibility(GONE);
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
        // dialog
//        createItemDialog(event);

        // bottom sheet
        createItemBottomSheetDialog(event);
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
    }

    private void createItemBottomSheetDialog(Event event) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activityContext, R.style.CustomBottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_rounded, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        TextView edit = sheetView.findViewById(R.id.tv_bottom_sheet_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityContext, AddEventActivity.class);
                intent.putExtra(EXTRA_EVENT_ID, event.getId());
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });

        TextView bookmark = sheetView.findViewById(R.id.tv_bottom_sheet_bookmark);
        bookmark.setText(event.isBookmarked() ? getString(R.string.bottom_sheet_action_remove_bookmark) : getString(R.string.bottom_sheet_action_bookmark));
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEventBookmark(event);
                bottomSheetDialog.dismiss();
            }
        });

        TextView share = sheetView.findViewById(R.id.tv_bottom_sheet_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(activityContext, "Share", Toast.LENGTH_SHORT).show();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.bottom_sheet_msg_share_event, event.getTitle()));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                bottomSheetDialog.dismiss();
            }
        });

        TextView addCalendar = sheetView.findViewById(R.id.tv_bottom_sheet_calendar);
        addCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent calendarIntent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                        .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                startActivity(calendarIntent);
            }
        });

        TextView delete = sheetView.findViewById(R.id.tv_bottom_sheet_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEvent(event);
                bottomSheetDialog.dismiss();
            }
        });
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
