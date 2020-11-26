package com.example.beever.feature;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.admin.MainActivity;
import com.example.beever.navigation.NavigationDrawer;

import java.util.ArrayList;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public static final String TAG = "ToDoAdapter";

    private Activity context;
    protected Button toDoButton;
    protected CheckBox toDoCheckBox;

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
     * @param context
     */
    public ToDoAdapter(Activity context) {
        this.context = context;
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

        toDoButton = view.findViewById(R.id.toDoButton);
        toDoCheckBox = view.findViewById(R.id.toDoCheckBox);

        toDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToDoDialogFragment toDoDialogFragment = new ToDoDialogFragment();
                // TODO: toDoDialogFragment.addToDo.setText("Edit To-Do");
                toDoDialogFragment.show(((NavigationDrawer) context).getSupportFragmentManager(), "EDIT_TO_DO");
            }
        });

        // TODO: Check for change for CheckBox. THIS CURRENTLY DOES NOT WORK
        toDoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String toDo = toDoButton.getText().toString();
                if (toDoCheckBox.isChecked()) {
                    ToDoFragment.toDoList.remove(toDo);
                    ToDoFragment.archivedList.add(toDo);
                } else {
                    if (!ToDoFragment.toDoList.contains(toDo)) {
                        ToDoFragment.toDoList.add(toDo);
                        ToDoFragment.archivedList.remove(toDo);
                    }
                }
            }
        });

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
        toDoButton.setText(ToDoFragment.toDoList.get(position));
    }

    /**
     * Returns the size of the dataset (invoked by layout manager)
     * @return
     */
    @Override
    public int getItemCount() {
        return ToDoFragment.toDoList.size();
    }
}
