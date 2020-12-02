package com.example.beever.feature;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TextEventAdapter extends RecyclerView.Adapter<TextEventAdapter.TextEventViewHolder>{

    private static final String TAG = "help";
    private ArrayList<EventEntry> dbEvents;
    static int moreThanDay;
    SimpleDateFormat sfDate = new SimpleDateFormat("dd MMM");
    SimpleDateFormat sfTime = new SimpleDateFormat("HH:mm");


    public static class TextEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView eventName;
        LinearLayout eventTiming;
        LinearLayout eventTiming_More;
        TextView startDate;
        TextView startTime;
        TextView endDate;
        TextView endTime;
        public TextEventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_title);
            eventTiming = itemView.findViewById(R.id.event);
            eventTiming_More = itemView.findViewById(R.id.event_more);
            if (moreThanDay == 1)  {
                startDate = itemView.findViewById(R.id.start_date_more);
                startTime = itemView.findViewById(R.id.start_time_more);
                endDate = itemView.findViewById(R.id.end_date_more);
                endTime = itemView.findViewById(R.id.end_time_more);
            } else {
                startDate = itemView.findViewById(R.id.start_date);
                startTime = itemView.findViewById(R.id.start_time);
                endTime = itemView.findViewById(R.id.end_time);
            }

            itemView.setOnClickListener(this);
        }

        public TextView getEventName() {
            return eventName;
        }

        public LinearLayout getEventTiming() {
            return eventTiming;
        }

        public LinearLayout getEventTiming_More() {
            return eventTiming_More;
        }

        public TextView getStartDate() {
            return startDate;
        }

        public TextView getEndDate() {
            return endDate;
        }

        public TextView getStartTime() {
            return startTime;
        }

        public TextView getEndTime() {
            return endTime;
        }

        @Override
        public void onClick(View v) {
            System.out.println("clicked");

        }
    }

    public TextEventAdapter(ArrayList<EventEntry> data) {
        this.dbEvents = data;
        Log.d(TAG, "TextEventAdapter: " + data.toString());
//        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        String start = sfDate.format(dbEvents.get(position).getStart_time().toDate());
        String end = sfDate.format(dbEvents.get(position).getEnd_time().toDate());
        if (start == end) {
            moreThanDay = 0;
        } else {
            moreThanDay = 1;
        }
        return moreThanDay;
    }

    @NonNull
    @Override
    public TextEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_card,parent,false);

        return new TextEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextEventViewHolder holder, int position) {
        String eventName = dbEvents.get(position).getName();
        String startTime = sfTime.format(dbEvents.get(position).getStart_time().toDate());
        String startDate = sfDate.format(dbEvents.get(position).getStart_time().toDate());
        String endTime = sfTime.format(dbEvents.get(position).getEnd_time().toDate());
        String endDate = sfDate.format(dbEvents.get(position).getEnd_time().toDate());

        Log.d("CHECK ADAPTER", "info includes - " +eventName+", " +startTime+", " +startDate+", " +endTime+", " +endDate);

        if (moreThanDay == 1) {
            holder.getEndDate().setText(endDate);
            holder.getEventTiming().setVisibility(View.GONE);
            holder.getEventTiming_More().setVisibility(View.VISIBLE);
        } else {
            holder.getEventTiming().setVisibility(View.VISIBLE);
            holder.getEventTiming_More().setVisibility(View.GONE);
        }
        holder.getEventName().setText(eventName);
        holder.getStartDate().setText(startDate);
        holder.getStartTime().setText(startTime);
    }

    @Override
    public int getItemCount() {
        return dbEvents.size();
    }

}
