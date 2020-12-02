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
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public static final String TAG = "ToDoAdapter";

    private final ArrayList<TodoEntry> toDoList;
    private final Context context;

    private final ToDoHelper helper;

    /**
     * Constructor for ToDoAdapter
     * @param toDoList  to display the current to-dos
     * @param groupID   to specify the current group the user is looking at
     * @param context   to specify the context for dialog to show
     * @param manager   to change the fragments accordingly
     * @param expandableListDetail  just to initialise helper
     * @param toDoArchivedAdapter   just to initialise helper
     */
    public ToDoAdapter(ArrayList<TodoEntry> toDoList, String groupID, Context context, FragmentManager manager,
                       HashMap<String, List<TodoEntry>> expandableListDetail, ExpandableListAdapter toDoArchivedAdapter) {
        this.toDoList = toDoList;
        this.context = context;
        this.helper = new ToDoHelper(context, manager, toDoList, this, expandableListDetail , toDoArchivedAdapter, groupID);
    }

    /**
     * VIEWHOLDER CLASS : provides a reference to the view that we are looking at
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // components in the view
        protected TextView toDoTaskContent;
        protected TextView toDoDeadline;
        protected TextView toDoAssignedTo;

        public ViewHolder(View view) {
            super(view);
            this.toDoTaskContent = view.findViewById(R.id.toDoTaskContent);
            this.toDoDeadline = view.findViewById(R.id.toDoDeadline);
            this.toDoAssignedTo = view.findViewById(R.id.toDoAssignedTo);

            // set an OnClickListener for user to select from options
            // to mark to-do as completed or to view the full to-do with description
            view.setOnClickListener(this);

            // set OnLongClickListener to allow the user to delete a to-do
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getLayoutPosition();
                    TodoEntry todoEntry = toDoList.get(position);
                    // shows the delete alert dialog
                    helper.showDeleteAlertDialog(context, todoEntry, true, false);
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            TodoEntry todoEntry = toDoList.get(position);

            //Fade Out Nav Bar
            Utils utils = new Utils(v.getContext());
            utils.fadeOut();

            // shows the options alert dialog
            helper.showOptionsAlertDialog(context, todoEntry, utils);
        }

    }

    /**
     * onCreateViewHolder : helps to hold a new view of the list item
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        // sets the corresponding text of the to-dos
        toDoTaskContent.setText(toDo.getName());
        toDoAssignedTo.setText(toDo.getAssigned_to());

        // format the timestamp into a readable date dd-MM
        Timestamp deadline = toDo.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd MMM");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());
        toDoDeadline.setText(deadlineStr);

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
