package com.example.beever.feature;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.TodoEntry;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public static final String TAG = "ToDoAdapter";

    private ArrayList<TodoEntry> toDoList;
    private Context context;

    private Utils utils;
    private ToDoHelper helper;

    /**
     * Initialise the toDoList with strings from Firebase
     * @param toDoList
     */
    public ToDoAdapter(ArrayList<TodoEntry> toDoList, String groupID, Context context, FragmentManager manager,
                       List<TodoEntry> archivedList, ExpandableListAdapter toDoArchivedAdapter) {
        this.toDoList = toDoList;
        this.context = context;
        this.helper = new ToDoHelper(context, manager, toDoList, this, archivedList, toDoArchivedAdapter, groupID);
    }

    /**
     * Provides a reference to the type of views that you are using
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView toDoTaskContent;
        protected TextView toDoDeadline;
        protected TextView toDoAssignedTo;

        public ViewHolder(View view) {
            super(view);
            this.toDoTaskContent = view.findViewById(R.id.toDoTaskContent);
            this.toDoDeadline = view.findViewById(R.id.toDoDeadline);
            this.toDoAssignedTo = view.findViewById(R.id.toDoAssignedTo);

            view.setOnClickListener(this);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getLayoutPosition();
                    TodoEntry todoEntry = toDoList.get(position);
                    helper.showDeleteAlertDialog(context, todoEntry, true, null);
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            TodoEntry todoEntry = toDoList.get(position);

            //Fade Out Nav Bar
            utils = new Utils(v.getContext());
            utils.fadeOut();

            helper.showOptionsAlertDialog(context, todoEntry, utils);
        }

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

        TextView toDoTaskContent = holder.toDoTaskContent;
        TextView toDoDeadline = holder.toDoDeadline;
        TextView toDoAssignedTo = holder.toDoAssignedTo;

        TodoEntry toDo = toDoList.get(position);

        toDoTaskContent.setText(toDo.getName());
        Timestamp deadline = toDo.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd-MM");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());
        toDoDeadline.setText(deadlineStr);
        toDoAssignedTo.setText(toDo.getAssigned_to());

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
