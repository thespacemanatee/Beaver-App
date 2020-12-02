package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.beever.R;
import com.example.beever.database.TodoEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

public class ToDoViewFragment extends Fragment {

    private final TodoEntry todoEntry;
    private final ToDoAdapter adapter;
    private final ToDoHelper helper;

    public ToDoViewFragment(TodoEntry todoEntry, ToDoAdapter adapter, ToDoHelper helper) {
        this.todoEntry = todoEntry;
        this.adapter = adapter;
        this.helper = helper;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((NavigationDrawer) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("To-Do View");

        View rootView = inflater.inflate(R.layout.fragment_to_do_view, container, false);
        String TAG = "ToDoViewFragment";
        rootView.setTag(TAG);

        // components in the view
        TextView toDoFullTaskTitle = rootView.findViewById(R.id.toDoFullTaskTitle);
        TextView toDoFullTaskDescr = rootView.findViewById(R.id.toDoFullTaskDescr);
        TextView toDoFullTaskAssignedTo = rootView.findViewById(R.id.toDoFullTaskAssignedTo);
        TextView toDoFullTaskDueDate = rootView.findViewById(R.id.toDoFullTaskDueDate);

        Button toDoFullBackBtn = rootView.findViewById(R.id.toDoFullBackBtn);
        Button toDoFullDeleteBtn = rootView.findViewById(R.id.toDoFullDeleteBtn);

        // setting the text in the components from todoEntry
        toDoFullTaskTitle.setText(todoEntry.getName());
        toDoFullTaskDescr.setText(todoEntry.getDescription());
        toDoFullTaskAssignedTo.setText(todoEntry.getAssigned_to());
        // formatting the timestamp to human readable format
        Timestamp deadline = todoEntry.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd MMM YYYY");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());
        toDoFullTaskDueDate.setText(deadlineStr);

        // goes back to previous fragment
        toDoFullBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getFragmentManager() != null;
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Nothing to return to!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // deletes the to do
        toDoFullDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.showDeleteAlertDialog(getContext(), todoEntry, true, true);
            }
        });

        return rootView;
    }

    // creates a new instance of the fragment
    public static ToDoViewFragment newInstance(TodoEntry todoEntry, ToDoAdapter adapter, ToDoHelper helper) {
        return new ToDoViewFragment(todoEntry, adapter, helper);
    }

}
