package com.example.beever.feature;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.beever.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class AddEventFragment extends Fragment {

    private static final String TAG = "AddEventFragment";

    private EditText mInput;
    private LinearLayout eventStart, eventEnd, addEventButtons;
    private TextView startDateTime, endDateTime;
    private Button cancel, save;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addevent, container, false);
        mInput = root.findViewById(R.id.eventTitle);
        eventStart = root.findViewById(R.id.startEvent);
        startDateTime = eventStart.findViewById(R.id.start_datetime);
        eventEnd = root.findViewById(R.id.endEvent);
        endDateTime = eventEnd.findViewById(R.id.end_datetime);
        addEventButtons = root.findViewById(R.id.add_event_buttons);
        cancel = addEventButtons.findViewById(R.id.cancel_button);
        save = addEventButtons.findViewById(R.id.save_button);

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        startDateTime.setText(day + "/" + (month + 1) + "/" + year);
        endDateTime.setText(day + "/" + (month + 1) + "/" + year);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: closing dialog");
                String input = mInput.getText().toString();
                if (input.isEmpty()) {
                    Toast.makeText(getContext(), "No information added. Event not saved.", Toast.LENGTH_SHORT).show();
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
                getFragmentManager().popBackStackImmediate();
            }

        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");
                String input = mInput.getText().toString();
                if (input.isEmpty()){
                    Toast.makeText(getContext(),"No information added. Event not saved.", Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        eventStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDateTime.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(cldr.getTimeInMillis());
                picker.show();
            }
        });

        eventEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDateTime.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(cldr.getTimeInMillis());
                picker.show();
            }
        });
        return root;
    }
}
