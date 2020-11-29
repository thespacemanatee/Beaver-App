package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;
import com.example.beever.database.UserEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ToDoDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener{
    public static final String TAG = "TO_DO_DIALOG_SPINNER";
    public static final String SPINNER = "Spinner set up successfully";

    protected View parentView;
    protected Spinner toDoDialogSpinner;
    protected EditText toDoDialogTask;
    protected EditText toDoDialogDescription;
    protected Button toDoDialogDate;
    protected TextView addToDo;
    protected int year, month, day;
    protected List<String> groupMembers;

    private String groupID;
    private GroupEntry.GetGroupEntry groupEntry;

    protected String assignedTo;
    protected String taskTitle;
    protected String taskDescr;
    protected Date dueDate;

    public ToDoDialogFragment(String groupID) {
        this.groupID = groupID;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        initGroupMembers();

        parentView = layoutInflater.inflate(R.layout.fragment_to_do_dialog, null);
        toDoDialogSpinner = parentView.findViewById(R.id.toDoDialogSpinner);
        toDoDialogTask = parentView.findViewById(R.id.toDoDialogTask);
        toDoDialogDescription = parentView.findViewById(R.id.toDoDialogDescription);
        toDoDialogDate = parentView.findViewById(R.id.toDoDialogDate);
        addToDo = parentView.findViewById(R.id.add_to_do);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.list);

        // setting the spinner for assigning to group members
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentView.getContext(), R.layout.spinner_item_dialog, groupMembers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        toDoDialogSpinner.setAdapter(adapter);
        toDoDialogSpinner.setOnItemSelectedListener(this);
        Log.d(TAG, SPINNER);

        // setting calendar when the due date button is pressed
        toDoDialogDate.setOnClickListener(v -> {
            // get current date
            final Calendar c = Calendar.getInstance(TimeZone.getDefault());
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                String setDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                toDoDialogDate.setText(setDate);
                dueDate = new GregorianCalendar(year, month - 1, dayOfMonth).getTime();
            }, year, month, day);

            // sets minimum date to current day
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

            datePickerDialog.show();
        });

        builder.setView(parentView)
                .setPositiveButton("Add", (dialog, which) -> {
                    taskTitle = toDoDialogTask.getText().toString();
                    taskDescr = toDoDialogDescription.getText().toString();
                    assignedTo = toDoDialogSpinner.getSelectedItem().toString();
                    if (dueDate.toString().isEmpty() || taskTitle.isEmpty() || assignedTo.isEmpty()) {
                        Toast.makeText(getContext(), "All fields except description must be filled in!", Toast.LENGTH_SHORT).show();
                    } else {
                        addNewToDo(taskTitle, taskDescr, assignedTo, dueDate);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    ToDoDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }

    private void initGroupMembers() {
        // TODO: get group members from firebase
        Log.d("INIT GROUP MEMBERS", groupID);
        groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                groupMembers = new ArrayList<>();

                if (isSuccessful()) {
                    List<Object> member_list = getResult().getMember_list();
                    for (Object o : member_list) {
                        groupMembers.add((String) o);
                    }
                }
            }
        };

        groupEntry.start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void addNewToDo(String taskTitle, String taskDescr, String assignedTo, Date dueDate) {
        TodoEntry newToDo = new TodoEntry(taskTitle, taskDescr, assignedTo, new Timestamp(dueDate), groupID);
        // TODO
        groupEntry.getResult().modifyEventOrTodo(false, true, true, newToDo);
    }
}
