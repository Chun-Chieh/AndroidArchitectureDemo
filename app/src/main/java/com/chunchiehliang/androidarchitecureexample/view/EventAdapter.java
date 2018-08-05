package com.chunchiehliang.androidarchitecureexample.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chunchiehliang.androidarchitecureexample.R;
import com.chunchiehliang.androidarchitecureexample.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Chun-Chieh Liang on 8/1/18.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static final String DATE_FORMAT = "MM/dd/yyyy";

    private static final int DATE_TODAY = 0;
    private static final int DATE_PAST = -1;

    private Context mContext;
    private List<Event> mEventList;

    final private ItemClickListener mItemClickListener;

    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());


    EventAdapter(Context context, ItemClickListener clickListener) {
        mContext = context;
        mItemClickListener = clickListener;
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event, parent, false);
        return new EventViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        final Event event = mEventList.get(position);
        holder.mTextViewTitle.setText(event.getTitle());
        holder.mTextViewDescription.setText(event.getDescription());
        holder.mTextViewDate.setText(dateFormat.format(event.getDate()));

        int dayDiff = getDayDiff(event.getDate());
        holder.mTextViewRemainDay.setText(getDayString(dayDiff));
        holder.mTextViewRemainDay.setTextColor(getDayColor(dayDiff));

        if (event.isBookmarked()) {
            holder.mImageBookMark.setVisibility(View.VISIBLE);
        } else {
            holder.mImageBookMark.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (mEventList == null) {
            return 0;
        }
        return mEventList.size();
    }


    public List<Event> getEventList() {
        return mEventList;
    }


    public void setEventList(List<Event> events) {
        mEventList = events;
        notifyDataSetChanged();
    }


    private int getDayDiff(Date eventDate) {

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(new Date());
        currentCal.set(Calendar.HOUR_OF_DAY, 0);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        currentCal.set(Calendar.MILLISECOND, 0);

        long unit = 1000 * 60 * 60 * 24;
        long dayDiff = (eventDate.getTime() - currentCal.getTime().getTime()) / unit;
        Log.d("Adapter", "Result : " + dayDiff);
        return (int) dayDiff;
    }


    private String getDayString(int dayDiff) {

        if (dayDiff == DATE_TODAY) {
            return mContext.getResources().getString(R.string.day_today);
        } else if (dayDiff < 0) {
            return mContext.getResources().getString(R.string.day_past);
        } else {
            return mContext.getResources().getQuantityString(R.plurals.remain_day, dayDiff, dayDiff);
        }
    }


    private int getDayColor(int dayDiff) {
        int dayColorResourceId;

        if (dayDiff <= DATE_PAST) {
            dayColorResourceId = R.color.colorPast;
        } else if (dayDiff == DATE_TODAY) {
            dayColorResourceId = R.color.colorToday;
        } else if (dayDiff <= 3) {
            dayColorResourceId = R.color.color1Week;
        } else if (dayDiff <= 14) {
            dayColorResourceId = R.color.color2Week;
        } else if (dayDiff <= 21) {
            dayColorResourceId = R.color.color3Week;
        } else if (dayDiff <= 30) {
            dayColorResourceId = R.color.color1Month;
        } else if (dayDiff <= 60) {
            dayColorResourceId = R.color.color2Month;
        } else if (dayDiff <= 180) {
            dayColorResourceId = R.color.color6Month;
        } else {
            dayColorResourceId = R.color.colorDefault;
        }
        return ContextCompat.getColor(mContext, dayColorResourceId);
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);

        void onItemLongClickListener(Event event);
    }


    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView mTextViewTitle, mTextViewDescription, mTextViewDate, mTextViewRemainDay;
        ImageView mImageBookMark;

        EventViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextViewTitle = itemView.findViewById(R.id.tv_event_title);
            mTextViewDescription = itemView.findViewById(R.id.tv_event_description);
            mTextViewDate = itemView.findViewById(R.id.tv_event_date);
            mTextViewRemainDay = itemView.findViewById(R.id.tv_event_remain_day);
            mImageBookMark = itemView.findViewById(R.id.image_bookmark);
        }

        @Override
        public void onClick(View view) {
            int eventId = mEventList.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(eventId);
        }

        @Override
        public boolean onLongClick(View view) {
            Event event = mEventList.get(getAdapterPosition());
            mItemClickListener.onItemLongClickListener(event);
            return true;
        }


    }
}
