package com.chunchiehliang.androidarchitecureexample.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.util.List;

/**
 * @author Chun-Chieh Liang on 8/1/18.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private List<Book> mBookList;

    // Provide a suitable constructor (depends on the kind of dataset)
    BookAdapter(List<Book> bookList) {
        mBookList = bookList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Book book = mBookList.get(position);
        holder.mTextViewTitle.setText(book.getTitle());
        holder.mTextViewAuthor.setText(book.getAuthor());
    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewTitle, mTextViewAuthor;

        ViewHolder(View view) {
            super(view);
            mTextViewTitle = view.findViewById(R.id.tv_book_title);
            mTextViewAuthor = view.findViewById(R.id.tv_book_author);
        }
    }
}
