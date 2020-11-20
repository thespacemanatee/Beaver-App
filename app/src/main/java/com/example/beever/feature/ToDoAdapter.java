package com.example.beever.feature;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;

import java.util.ArrayList;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public static final String TAG = "ToDoAdapter";

    private ArrayList<String> toDoList;

    /**
     * Provides a reference to the type of views that you are using
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout toDoTaskView;

        public ViewHolder(View view) {
            super(view);

            // define Click Listener for view
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, getAdapterPosition() + " was clicked.");
                }
            });
            toDoTaskView = view.findViewById(R.id.toDoTaskView);
        }

        public LinearLayout getToDoTaskView() {
            return toDoTaskView;
        }
    }

    /**
     * Initialise the toDoList with strings from Firebase
     * @param toDoList
     */
    public ToDoAdapter(ArrayList<String> toDoList) {
        this.toDoList = toDoList;
    }

    /**
     * Creates new views (invoked by layout manager)
     * @param parent parent viewgroup
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // creates a new view of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_to_do_task_, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Replaces the contents of the view (invoked by layout manager)
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get element from dataset at the specified position and replace
        // the contents of the view with that element
        Log.d(TAG, "To Do List " + position + " set.");
       Button toDoButton = holder.getToDoTaskView().findViewById(R.id.toDoButton);
       toDoButton.setText(toDoList.get(position));
    }

    /**
     * Returns the size of the dataset (invoked by layout manager)
     * @return
     */
    @Override
    public int getItemCount() {
        return toDoList.size();
    }
}
