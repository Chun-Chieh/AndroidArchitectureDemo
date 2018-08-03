package com.chunchiehliang.androidarchitecureexample.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.model.Book;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author Chun-Chieh Liang on 8/1/18.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private static final String DATE_FORMAT = "MM/dd/yyyy";

    private Context mContext;
    private List<Book> mBookList;

    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());


    BookAdapter(Context context){
        mContext = context;
    }
    BookAdapter(List<Book> bookList) {
        mBookList = bookList;
    }

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
        holder.mTextViewPubDate.setText(dateFormat.format(book.getPublicationDate()));
    }

    @Override
    public int getItemCount() {
        if (mBookList == null) {
            return 0;
        }
        return mBookList.size();
    }


    public List<Book> getBookList() {
        return mBookList;
    }


    public void setBookList(List<Book> books){
        mBookList = books;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewTitle, mTextViewAuthor, mTextViewPubDate;

        ViewHolder(View view) {
            super(view);
            mTextViewTitle = view.findViewById(R.id.tv_book_title);
            mTextViewAuthor = view.findViewById(R.id.tv_book_author);
            mTextViewPubDate = view.findViewById(R.id.tv_pub_date);
        }
    }
}
