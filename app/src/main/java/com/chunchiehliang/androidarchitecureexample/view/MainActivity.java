package com.chunchiehliang.androidarchitecureexample.view;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    private BookAdapter mAdapter;

    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadBooks();

        iniViews();
    }

    private void iniViews() {
        mAdapter = new BookAdapter(bookList);

        mRecyclerView = findViewById(R.id.recycler_view_main);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Book removedBook = bookList.remove(position);
                mAdapter.notifyItemRemoved(position);

                Snackbar mSnackBar = Snackbar.make(findViewById(R.id.coordinator_main), getString(R.string.item_removed_string, removedBook.getTitle()), Snackbar.LENGTH_LONG);
                mSnackBar.setAction(R.string.undo_string, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookList.add(position, removedBook);
                        mAdapter.notifyItemInserted(position);
                    }
                });
                mSnackBar.show();
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                final ColorDrawable background = new ColorDrawable(Color.RED);
                background.setBounds((int) (viewHolder.itemView.getRight() + dX),
                        viewHolder.itemView.getTop(),
                        viewHolder.itemView.getRight(),
                        viewHolder.itemView.getBottom());
                background.draw(c);

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
                Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadBooks() {
        bookList = new ArrayList<>();
        bookList.add(new Book("Harry Potter", "JK Rowling"));
        bookList.add(new Book("Life", "Forrest Gump"));
    }
}
