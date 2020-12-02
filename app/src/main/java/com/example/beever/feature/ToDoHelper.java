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

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToDoHelper {

    private Context context;
    private FragmentManager fragmentManager;
    private ToDoAdapter adapter;
    private ArrayList<TodoEntry> toDoList;
    private List<TodoEntry> archivedList;
    private ExpandableListAdapter toDoArchivedAdapter;
    private String groupID;

    public ToDoHelper(Context context, FragmentManager fragmentManager,
                      ArrayList<TodoEntry> toDoList, ToDoAdapter adapter,
                      List<TodoEntry> archivedList, ExpandableListAdapter toDoArchivedAdapter, String groupID) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.toDoList = toDoList;
        this.adapter = adapter;
        this.archivedList = archivedList;
        this.toDoArchivedAdapter = toDoArchivedAdapter;
        this.groupID = groupID;
    }

    public void showOptionsAlertDialog(Context context, TodoEntry todoEntry, Utils utils) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Choose an Option: ");
        dialog.setMessage("To-Do: " + todoEntry.getName());

        dialog.setButton(Dialog.BUTTON_POSITIVE, "View Full To-Do", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ToDoViewFragment.newInstance(todoEntry, adapter, ToDoHelper.this))
                        .addToBackStack("ToDoFragment")
                        .commit();
            }
        });

        dialog.setButton(Dialog.BUTTON_NEUTRAL, "Mark as Completed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Completed", Toast.LENGTH_SHORT).show();
                utils.fadeIn();
                markAsCompleted(todoEntry);
            }
        });

        dialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                utils.fadeIn();
            }
        });

        dialog.show();
    }


    public void showDeleteAlertDialog(Context context, TodoEntry todoEntry, boolean isCurrent, HashMap<String, List<TodoEntry>> expandableListDetail) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Delete To-Do?");
        dialog.setMessage("To-Do Chosen: " + todoEntry.getName());

        dialog.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isCurrent) {
                    removeItem(todoEntry);
                } else {
                    removeCompleted(todoEntry, expandableListDetail);
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

    public void removeItem(TodoEntry todoEntry) {
        int currPosition = toDoList.indexOf(todoEntry);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, false, todoEntry);

                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "To-Do removed :)", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    toDoList.remove(todoEntry);
                    adapter.notifyItemRemoved(currPosition);

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

                } else {
                    Toast.makeText(context, "Cannot add to-do", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();
    }

    public void markAsCompleted(TodoEntry todoEntry) {
        GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    getResult().modifyEventOrTodo(false, true, false, todoEntry);
                    getResult().modifyEventOrTodo(false, false, true, todoEntry);

                    GroupEntry.SetGroupEntry setGroupEntry = new GroupEntry.SetGroupEntry(getResult(), groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            Toast.makeText(context, "Marked!", Toast.LENGTH_SHORT).show();
                        }
                    };

                    setGroupEntry.start();

                    toDoList.remove(todoEntry);
                    adapter.notifyDataSetChanged();

                    try {
                        archivedList.add(todoEntry);
                        toDoArchivedAdapter.notifyDataSetChanged();
                    } catch (NullPointerException e) {

                    }
                }
            }
        };

        getGroupEntry.start();
    }

    public void removeCompleted(TodoEntry todoEntry, HashMap<String, List<TodoEntry>> expandableListDetail) {
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

                    expandableListDetail.get("Completed").remove(todoEntry);
                    toDoArchivedAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(context, "Unable to remove from Completed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        groupEntry.start();
    }
}
