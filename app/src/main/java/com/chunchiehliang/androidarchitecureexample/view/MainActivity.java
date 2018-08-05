package com.chunchiehliang.androidarchitecureexample.view;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.chunchiehliang.androidarchitecureexample.AppExecutors;
import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.database.AppDatabase;
import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    private EventAdapter mAdapter;

    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iniViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void iniViews() {
        mRecyclerView = findViewById(R.id.recycler_view_main);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new MarginItemDecoration((int) (getResources().getDimension(R.dimen.md_16dp) / getResources().getDisplayMetrics().density)));

        mAdapter = new EventAdapter(this, this);

        mRecyclerView.setAdapter(mAdapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Event removedEvent = eventList.remove(position);
                mAdapter.notifyItemRemoved(position);

                // Delete the item from the database
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(getApplicationContext()).eventDao().deleteEvent(removedEvent);
                    }
                });


                Snackbar mSnackBar = Snackbar.make(findViewById(R.id.coordinator_main), getString(R.string.item_removed_string, removedEvent.getTitle()), Snackbar.LENGTH_LONG);
                mSnackBar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        eventList.add(position, removedEvent);
                        mAdapter.notifyItemInserted(position);

                        // Add it back to the database
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getInstance(getApplicationContext()).eventDao().insertEvent(removedEvent);
                            }
                        });
                    }
                });

                mSnackBar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);


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
                final ColorDrawable backgroundDelete = new ColorDrawable(getColor(R.color.materialRed));
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

        mFab = findViewById(R.id.fab_main);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadEvents() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                eventList = AppDatabase.getInstance(getApplicationContext()).eventDao().loadAllEvents();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setEventList(eventList);
                    }
                });
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        Toast.makeText(MainActivity.this, "click listener, event ID:" + itemId, Toast.LENGTH_SHORT).show();
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
                AppDatabase.getInstance(getApplicationContext()).eventDao().updateEvent(event);
                runOnUiThread(new Runnable() {
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
