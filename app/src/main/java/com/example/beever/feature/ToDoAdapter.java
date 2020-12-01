package com.example.beever.feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.admin.MainActivity;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public static final String TAG = "ToDoAdapter";

    private ArrayList<TodoEntry> toDoList = new ArrayList<>();
    private String groupID;
    private Context context;

    /**
     * Initialise the toDoList with strings from Firebase
     * @param toDoList
     */
    public ToDoAdapter(ArrayList<TodoEntry> toDoList, String groupID, Context context) {
        this.toDoList = toDoList;
        this.groupID = groupID;
        this.context = context;
    }

    /**
     * Provides a reference to the type of views that you are using
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected CheckBox toDoCheckBox;
        protected TextView toDoTaskContent;
        protected TextView toDoDeadline;
        protected TextView toDoAssignedTo;

        public ViewHolder(View view) {
            super(view);
            this.toDoCheckBox = view.findViewById(R.id.toDoCheckBox);
            this.toDoTaskContent = view.findViewById(R.id.toDoTaskContent);
            this.toDoDeadline = view.findViewById(R.id.toDoDeadline);
            this.toDoAssignedTo = view.findViewById(R.id.toDoAssignedTo);

            view.setOnClickListener(this);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getLayoutPosition();
                    TodoEntry todoEntry = toDoList.get(position);
                    showAlertDialog(context, "Delete To-Do?", todoEntry);
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            TodoEntry todoEntry = toDoList.get(position);
            Timestamp deadline = todoEntry.getDeadline();
            SimpleDateFormat sf = new SimpleDateFormat("dd-MM");
            sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
            String deadlineStr = sf.format(deadline.toDate());

            /*ToDoDialogFragment toDoDialogFragment = new ToDoDialogFragment(groupID, R.layout.fragment_to_do_edit);
            toDoDialogFragment.show(fragmentManager, TAG);*/

            // showAlertDialog(context, "Delete To-Do?", todoEntry);

            // TODO: Make intent and show different fragment that displays the details of the todo
        }
    }

    private void showAlertDialog(Context context, String message, TodoEntry todoEntry) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setMessage(message);

        dialog.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeItem(todoEntry);
            }
        });

        dialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void removeItem(TodoEntry todoEntry) {
        int currPosition = toDoList.indexOf(todoEntry);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, false, todoEntry);
                    notifyItemRemoved(currPosition);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Cannot remove to-do", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();
    }

    public void addItem(TodoEntry todoEntry) {
        Log.d("GROUP ID", groupID);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, true, todoEntry);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Cannot add to-do", Toast.LENGTH_SHORT).show();
                }

                // TODO
            }
        };

        groupEntry.start();
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
        CheckBox toDoCheckBox = holder.toDoCheckBox;

        TodoEntry toDo = toDoList.get(position);

        toDoTaskContent.setText(toDo.getName());
        Timestamp deadline = toDo.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd-MM");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());
        toDoDeadline.setText(deadlineStr);
        toDoAssignedTo.setText(toDo.getAssigned_to());

        toDoCheckBox.setOnCheckedChangeListener(null);

        toDoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    notifyItemRemoved(position);
                }
            }
        });
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
