package com.example.beever.feature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;

import java.util.ArrayList;

public class TextEventAdapter extends RecyclerView.Adapter<TextEventAdapter.TextEventViewHolder>{

    private ArrayList<Events> dataSet;
    Context mContext;
    int total_types;

    public static class TextEventViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        CardView cardView;

        public TextEventViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.textevent);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }

    public TextEventAdapter(ArrayList<Events>data, Context context) {
        this.dataSet = data;
        this.mContext = context;
        total_types = dataSet.size();
    }

    @NonNull
    @Override
    public TextEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_event,parent,false);
        return new TextEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextEventViewHolder holder, int position) {
        Events object = dataSet.get(position);
        if (object != null){
            ((TextEventViewHolder) holder).textView.setText(object.text);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
