package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.beever.R;

import java.util.Calendar;

public class ToDoDialogFragment extends DialogFragment {

    protected EditText toDoDialogAssign;
    protected EditText toDoDialogTask;
    protected EditText toDoDialogDate;
    protected int year, month, day;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();

        View parentView = layoutInflater.inflate(R.layout.fragment_to_do_dialog, null);
        toDoDialogAssign = parentView.findViewById(R.id.toDoDialogAssignTo);
        toDoDialogTask = parentView.findViewById(R.id.toDoDialogTask);
        toDoDialogDate = parentView.findViewById(R.id.toDoDialogDate);

        toDoDialogDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // get current date
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String setDate = dayOfMonth + "/" + month + "/" + year;
                        toDoDialogDate.setText(setDate);
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        builder.setView(parentView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // adds to do
                    toDoDialogListener.onDialogPositiveClick(ToDoDialogFragment.this);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    toDoDialogListener.onDialogNegativeClick(ToDoDialogFragment.this);
                    ToDoDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }

    /**
     * Interface for ToDoFragment to implement in order to receive event callbacks
     */
    public interface ToDoDialogListener {
        void onDialogPositiveClick(ToDoDialogFragment dialogFragment);
        void onDialogNegativeClick(ToDoDialogFragment dialogFragment);
    }

    // use instance of listener to deliver event callbacks
    ToDoDialogListener toDoDialogListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Verify that host activity implements the interface
        try {
            toDoDialogListener = (ToDoDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ToDoDialogListener");
        }
    }
}
