package com.example.beever.feature;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;

import java.util.ArrayList;

public class TextEventAdapter extends RecyclerView.Adapter<TextEventAdapter.TextEventViewHolder>{

    private static final String TAG = "help";
    private ArrayList<EventEntry> dataSet;
//    Context mContext;

    public static class TextEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textView;
        CardView cardView;

        public TextEventViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.text_event);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            System.out.println("clicked");

        }
    }

    public TextEventAdapter(ArrayList<EventEntry> data) {
        this.dataSet = data;
        Log.d(TAG, "TextEventAdapter: " + data.toString());
//        this.mContext = context;
    }

    @NonNull
    @Override
    public TextEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_event,parent,false);

        return new TextEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextEventViewHolder holder, int position) {
        EventEntry object = dataSet.get(position);
        if (object != null){
            ((TextEventViewHolder) holder).textView.setText(object.getName());
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
