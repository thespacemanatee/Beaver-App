package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ToDoHelper {

    // get required components in order to show dialogs, and
    // retrieve or put data into firestore
    private final Context context;
    private final FragmentManager fragmentManager;
    private final ToDoAdapter adapter;
    private final ArrayList<TodoEntry> toDoList;
    private final ExpandableListAdapter toDoArchivedAdapter;
    private final HashMap<String, List<TodoEntry>> expandableListDetail;
    private final String groupID;


    /**
     * Constructor for ToDoHelper
     * @param context   gets the current context in order to Toast
     * @param fragmentManager   gets fragment manager in order to perform fragment transactions
     * @param toDoList  gets toDoList to update toDoList and notify ToDoAdapter of changes
     * @param adapter   ToDoAdapter gets notified of changes in toDoList and modifies the view accordingly
     * @param expandableListDetail  expandableListDetail for the completed segment
     * @param toDoArchivedAdapter   toDoArchivedAdapter gets notified of changes in expandableListDetail and modifies view accordingly
     * @param groupID   specifies the group that the user is looking at
     */
    public ToDoHelper(Context context, FragmentManager fragmentManager,
                      ArrayList<TodoEntry> toDoList, ToDoAdapter adapter,
                      HashMap<String, List<TodoEntry>> expandableListDetail, ExpandableListAdapter toDoArchivedAdapter, String groupID) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.toDoList = toDoList;
        this.adapter = adapter;
        this.expandableListDetail = expandableListDetail;
        this.toDoArchivedAdapter = toDoArchivedAdapter;
        this.groupID = groupID;
    }

    /**
     * showOptionsAlertDialog : lets the user choose from certain options when a to-do is clicked
     * @param context
     * @param todoEntry
     * @param utils
     */
    public void showOptionsAlertDialog(Context context, TodoEntry todoEntry, Utils utils) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Choose an Option: ");
        dialog.setMessage("To-Do: " + todoEntry.getName());

        // allows the user to view full to-do inclusive of description
        dialog.setButton(Dialog.BUTTON_POSITIVE, "View Full To-Do", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do a fragment transaction to a new ToDoViewFragment that displays the full to-do
                // add the current fragment to back stack for the user to return to
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ToDoViewFragment.newInstance(todoEntry, adapter, ToDoHelper.this))
                        .addToBackStack("ToDoFragment")
                        .commit();
            }
        });

        // allows the user to mark the to-do as completed
        dialog.setButton(Dialog.BUTTON_NEUTRAL, "Mark as Completed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                utils.fadeIn();
                // removes the to-do from current list and adds to completed list
                markAsCompleted(todoEntry);
            }
        });

        // for the user to dismiss the dialog
        dialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                utils.fadeIn();
            }
        });

        dialog.show();
    }


    /**
     * showDeleteAlertDialog : appears on a long press on a current to-do or a click on a completed to-do
     * @param context
     * @param todoEntry
     * @param isCurrent
     */
    public void showDeleteAlertDialog(Context context, TodoEntry todoEntry, boolean isCurrent, boolean isFullView) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Delete To-Do?");
        dialog.setMessage("To-Do Chosen: " + todoEntry.getName());

        dialog.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isCurrent) {
                    removeItem(todoEntry);
                } else {
                    removeCompleted(todoEntry);
                }

                if (isFullView) {
                    assert fragmentManager != null;
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, new ToDoFragment())
                            .commit();
                }
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


    /**
     * removeItem : removes the current to-do from firebase
     * @param todoEntry
     */
    public void removeItem(TodoEntry todoEntry) {
        int currPosition = toDoList.indexOf(todoEntry);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    // removes the to-do locally first
                    getResult().modifyEventOrTodo(false, true, false, todoEntry);

                    // removes the to-do from firebase
                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "To-Do removed :)", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    // remove the todoEntry from toDoList and notify adapter to display changes
                    toDoList.remove(todoEntry);
                    adapter.notifyItemRemoved(currPosition);

                } else {
                    Toast.makeText(context, "Cannot remove to-do", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();

    }

    /**
     * addItem : adds a current to-do to firestore
     * triggered upon pressing the add button in ToDoDialogFragment
     * @param todoEntry
     */
    public void addItem(TodoEntry todoEntry, RecyclerView recyclerView) {
        Log.d("GROUP ID", groupID);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, true, todoEntry);

                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "To-Do added :)", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    toDoList.add(0, todoEntry);
                    adapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);

                } else {
                    Toast.makeText(context, "Cannot add to-do", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();
    }


    /**
     * markAsCompleted : removes the to-do from the current to-do list in firestore, and adds it to the past to-dos list
     * @param todoEntry
     */
    public void markAsCompleted(TodoEntry todoEntry) {
        GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, false, todoEntry);
                    getResult().modifyEventOrTodo(false, false, true, todoEntry);

                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "Completed!", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    // notify changes to adapter after modifying the list to display changes
                    toDoList.remove(todoEntry);
                    adapter.notifyDataSetChanged();

                    Objects.requireNonNull(expandableListDetail.get("Completed")).add(todoEntry);
                    expandableListDetail.get("Completed").sort(new ToDoComparator());
                    toDoArchivedAdapter.notifyDataSetChanged();
                }
            }
        };

        getGroupEntry.start();
    }


    /**
     * Removes a completed to-do from the past list in firestore
     * @param todoEntry
     */
    public void removeCompleted(TodoEntry todoEntry) {
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, false, false, todoEntry);

                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "Removed from Completed", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    Objects.requireNonNull(expandableListDetail.get("Completed")).remove(todoEntry);
                    toDoArchivedAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(context, "Unable to remove from Completed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();
    }
}
