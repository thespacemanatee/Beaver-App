package com.example.beever.feature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class GapAdapter extends RecyclerView.Adapter<GapAdapter.ViewHolder> {

    private ArrayList<Timestamp> adapterTimestamps;
    private OnTimestampListener mOnTimestampListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView timestampTitle;
        OnTimestampListener onTimestampListener;


        public ViewHolder(@NonNull View itemView, OnTimestampListener onTimestampListener) {
            super(itemView);
            timestampTitle = itemView.findViewById(R.id.timestamp_text_title);
            itemView.setOnClickListener(this);
            this.onTimestampListener = onTimestampListener;
        }

        public TextView getTimestampTitle() {
            return timestampTitle;
        }

        @Override
        public void onClick(View v) {
            onTimestampListener.onTimestampClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnTimestampListener {
        void onTimestampClick(int position);
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param timestamps ArrayList<Timestamp> containing the data to populate views to be used
     * by RecyclerView.
     */
    public GapAdapter(ArrayList<Timestamp> timestamps, OnTimestampListener onTimestampListener) {
        adapterTimestamps = timestamps;
        this.mOnTimestampListener = onTimestampListener;
    }

    @NonNull
    @Override
    public GapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gap_finder_cells, parent, false);

        return new GapAdapter.ViewHolder(itemView, mOnTimestampListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GapAdapter.ViewHolder holder, int position) {
        holder.getTimestampTitle().setText(adapterTimestamps.get(position).toDate().toString().substring(0, 16));

    }

    @Override
    public int getItemCount() {
        return adapterTimestamps.size();
    }
}
