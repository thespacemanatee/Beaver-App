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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
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
    protected Button toDoDialogDate;
    protected TextView addToDo;
    protected int year, month, day;
    protected List<String> groupMembers = new ArrayList<>();

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    protected String assignedTo;
    protected String taskDetails;
    protected String dueDate;
    protected String TASK_KEY = "taskDetails";
    protected String ASSIGNED_KEY = "assignedTo";
    protected String DUE_DATE_KEY = "dueDate";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        initGroupMembers();

        parentView = layoutInflater.inflate(R.layout.fragment_to_do_dialog, null);
        toDoDialogSpinner = parentView.findViewById(R.id.toDoDialogSpinner);
        toDoDialogTask = parentView.findViewById(R.id.toDoDialogTask);
        toDoDialogDate = parentView.findViewById(R.id.toDoDialogDate);
        addToDo = parentView.findViewById(R.id.add_to_do);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.list);

        FirebaseUser fUser = fAuth.getCurrentUser();

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
            }, year, month, day);

            // sets minimum date to current day
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

            datePickerDialog.show();
        });

        builder.setView(parentView)
                .setPositiveButton("Add", (dialog, which) -> {
                    dueDate = toDoDialogDate.getText().toString();
                    taskDetails = toDoDialogTask.getText().toString();
                    assignedTo = toDoDialogSpinner.getSelectedItem().toString();
                    if (dueDate.isEmpty() || taskDetails.isEmpty() || assignedTo.isEmpty()) {
                        Toast.makeText(getContext(), "All fields must be filled in!", Toast.LENGTH_SHORT).show();
                    } else {
                        addNewToDo(taskDetails, assignedTo, dueDate);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    ToDoDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }

    private void initGroupMembers() {
        // TODO: get group members from firebase
        groupMembers.add("Claudia");
        groupMembers.add("Chee Kit");
        groupMembers.add("Jun Hao");
        groupMembers.add("Sean");
        groupMembers.add("Xing Yi");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void addNewToDo(String taskDetails, String assignedTo, String dueDate) {
        Map<String, String> newToDo = new HashMap<>();
        newToDo.put(TASK_KEY, taskDetails);
        newToDo.put(ASSIGNED_KEY, assignedTo);
        newToDo.put(DUE_DATE_KEY, dueDate);
        // TODO
        fStore.collection("groups").document("??").set(newToDo);
    }
}
