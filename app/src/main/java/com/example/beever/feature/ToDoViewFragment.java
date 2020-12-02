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
import androidx.fragment.app.FragmentTransaction;

import com.example.beever.R;
import com.example.beever.database.TodoEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

public class ToDoViewFragment extends Fragment {

    private static String TAG = "ToDoViewFragment";

    private View rootView;
    private TextView toDoFullTaskTitle;
    private TextView toDoFullTaskDescr;
    private TextView toDoFullTaskAssignedTo;
    private TextView toDoFullTaskDueDate;
    private Button toDoFullBackBtn;
    private Button toDoFullDeleteBtn;

    private TodoEntry todoEntry;
    private ToDoAdapter adapter;
    private ToDoHelper helper;

    public ToDoViewFragment(TodoEntry todoEntry, ToDoAdapter adapter, ToDoHelper helper) {
        this.todoEntry = todoEntry;
        this.adapter = adapter;
        this.helper = helper;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((NavigationDrawer) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("To-Do View");

        rootView = inflater.inflate(R.layout.fragment_to_do_view, container, false);
        rootView.setTag(TAG);

        toDoFullTaskTitle = rootView.findViewById(R.id.toDoFullTaskTitle);
        toDoFullTaskDescr = rootView.findViewById(R.id.toDoFullTaskDescr);
        toDoFullTaskAssignedTo = rootView.findViewById(R.id.toDoFullTaskAssignedTo);
        toDoFullTaskDueDate = rootView.findViewById(R.id.toDoFullTaskDueDate);

        toDoFullBackBtn = rootView.findViewById(R.id.toDoFullBackBtn);
        toDoFullDeleteBtn = rootView.findViewById(R.id.toDoFullDeleteBtn);

        toDoFullTaskTitle.setText(todoEntry.getName());
        toDoFullTaskDescr.setText(todoEntry.getDescription());
        toDoFullTaskAssignedTo.setText(todoEntry.getAssigned_to());

        Timestamp deadline = todoEntry.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd-MM-YYYY");
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());
        toDoFullTaskDueDate.setText(deadlineStr);

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

        toDoFullDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlertDialog(getContext(), todoEntry);
            }
        });

        return rootView;
    }

    public static ToDoViewFragment newInstance(TodoEntry todoEntry, ToDoAdapter adapter, ToDoHelper helper) {
        return new ToDoViewFragment(todoEntry, adapter, helper);
    }

    private void showDeleteAlertDialog(Context context, TodoEntry todoEntry) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Delete To-Do?");
        dialog.setMessage("To-Do Chosen: " + todoEntry.getName());

        dialog.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                helper.removeItem(todoEntry);
                adapter.notifyDataSetChanged();

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ToDoFragment())
                        .commit();
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
}
