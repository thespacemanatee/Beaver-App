package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;
import com.example.beever.database.UserEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class ToDoDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener{
    // set some tags
    public static final String TAG = "TO_DO_DIALOG_SPINNER";
    public static final String SPINNER = "Spinner set up successfully";

    // view components plus local data
    protected View parentView;
    protected Spinner toDoDialogSpinner;
    protected TextInputLayout toDoDialogTask, toDoDialogDescription;
    protected Button toDoDialogDate;
    protected TextView addToDo;
    protected RecyclerView recyclerView;
    protected int year, month, day;
    protected List<String> groupMembers = new ArrayList<>();
    protected ArrayAdapter<String> spinnerAdapter;

    private final ToDoHelper helper;

    private final String groupID;

//    protected String assignedTo;
//    protected String taskTitle;
//    protected String taskDescr;
    protected Date dueDate;

    /**
     * Constructor for ToDoDialogFragment
     * @param groupID   to see what group the user is looking at
     * @param helper    helps with data retrieval from firestore
     */
    public ToDoDialogFragment(String groupID, ToDoHelper helper, RecyclerView recyclerView) {
        this.groupID = groupID;
        this.helper = helper;
        this.recyclerView = recyclerView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();

        // get group members in the group to populate spinner
        initGroupMembers();

        // inflate the layout and find the required components
        parentView = layoutInflater.inflate(R.layout.fragment_to_do_dialog, null);
        toDoDialogSpinner = parentView.findViewById(R.id.toDoDialogSpinner);
        toDoDialogTask = parentView.findViewById(R.id.toDoDialogTask);
        toDoDialogDescription = parentView.findViewById(R.id.toDoDialogDescription);
        toDoDialogDate = parentView.findViewById(R.id.toDoDialogDate);
        addToDo = parentView.findViewById(R.id.add_to_do);
    }


    /**
     * onCreateDialog : specifies what should be done when the dialog is first created
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // start an alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.list);

        // set OnItemSelectedListener for spinner to know which group member
        // the to-do is assigned to
        toDoDialogSpinner.setOnItemSelectedListener(this);
        Log.d(TAG, SPINNER);

        // setting spinner to group members list
        spinnerAdapter = new ArrayAdapter<>(parentView.getContext(), R.layout.spinner_item_dialog, groupMembers);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        toDoDialogSpinner.setAdapter(spinnerAdapter);

        // setting calendar when the due date button is pressed
        toDoDialogDate.setOnClickListener(v -> {
            // get current date
            final Calendar c = Calendar.getInstance(TimeZone.getDefault());
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // create a DatePickerDialog for the user to input a date and format accordingly
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                String setDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                toDoDialogDate.setText(setDate);
                dueDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
            }, year, month, day);

            // sets minimum date to current day
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

            datePickerDialog.show();
        });

        // adds positive and negative buttons to the dialog for the user to select
        builder.setView(parentView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // positive button adds the to-do
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // negative button just dismisses the dialog
                    Objects.requireNonNull(ToDoDialogFragment.this.getDialog()).cancel();
                });

        return builder.create();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog != null)
        {
            Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO Do something
                    String task = toDoDialogTask.getEditText().getText().toString();
                    String description = toDoDialogDescription.getEditText().getText().toString();
                    String assignedTo = toDoDialogSpinner.getSelectedItem().toString();
                    if (task.isEmpty()) {
                        toDoDialogTask.setError("Task title cannot be empty");
                    } else {
                        toDoDialogTask.setError(null);
                        toDoDialogTask.setErrorEnabled(false);
                    }
                    if (description.isEmpty()) {
                        toDoDialogDescription.setError("Task description cannot be empty");
                    } else {
                        toDoDialogDescription.setError(null);
                        toDoDialogDescription.setErrorEnabled(false);
                    }
                    if (dueDate == null) {
                        Toast.makeText(getContext(), "Due date cannot be empty", Toast.LENGTH_SHORT).show();
                    }

                    //Dismiss once everything is OK.
                    if (!(task.isEmpty() || description.isEmpty() || dueDate == null)) {
                        addNewToDo(task, description, assignedTo, dueDate);
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void initGroupMembers() {
        Log.d("INIT GROUP MEMBERS", groupID);
        GroupEntry.GetGroupEntry groupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    try {
                        // get the member list for the current group specified by groupID
                        List<Object> member_list = getResult().getMember_list();
                        Log.d("TO DO DIALOG", member_list.toString());

                        // for every member, get the member's name and add to groupMembers list
                        // notify spinner that there are changes to the dataset
                        for (Object o : member_list) {
                            UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry((String) o, 5000) {
                                @Override
                                public void onPostExecute() {
                                    groupMembers.add(getResult().getName());
                                    spinnerAdapter.notifyDataSetChanged();
                                }
                            };
                            getUserEntry.start();
                        }

                    } catch (NullPointerException e) {
                        // invoked when member_list in firestore is null which means
                        // no group members found
                        Toast.makeText(getContext(), "No Group Members found :(", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Not Successful", Toast.LENGTH_SHORT).show();
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
        // creates a new ToDoEntry
        TodoEntry newToDo = new TodoEntry(taskTitle, taskDescr, assignedTo, new Timestamp(dueDate), groupID);
        // uses helper to add this to-do
        helper.addItem(newToDo, recyclerView);

    }
}
