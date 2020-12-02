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
import com.example.beever.database.EventEntry;
import com.example.beever.database.UserEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class AddEventFragment extends Fragment {

    private static final String TAG = "AddEventFragment";

    private EditText mInput,mDescription;
    private LinearLayout eventStart, eventEnd, addEventButtons;
    private TextView startDateTime, endDateTime;
    private Button cancel, save;
    private Date start, end;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private UserEntry userEntry;
    private String USER_ID;
    private Utils utils;
    private Calendar calendar = Calendar.getInstance();
    private int selectedDay, selectedMonth, selectedYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addevent, container, false);
        mInput = root.findViewById(R.id.eventTitle);
        mDescription = root.findViewById(R.id.description);
        eventStart = root.findViewById(R.id.startEvent);
        startDateTime = eventStart.findViewById(R.id.start_datetime);
        eventEnd = root.findViewById(R.id.endEvent);
        endDateTime = eventEnd.findViewById(R.id.end_datetime);
        addEventButtons = root.findViewById(R.id.add_event_buttons);
        cancel = addEventButtons.findViewById(R.id.cancel_button);
        save = addEventButtons.findViewById(R.id.save_button);

        Bundle bundle = this.getArguments();
        selectedDay = bundle.getInt("selectedDay");
        selectedMonth = bundle.getInt("selectedMonth");
        selectedYear = bundle.getInt("selectedYear");

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        USER_ID = firebaseUser.getUid();

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        start = new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime();
        end = new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime();

        startDateTime.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
        endDateTime.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: closing dialog");
                utils = new Utils(v.getContext());
                String input = mInput.getText().toString();
                if (input.isEmpty()) {
                    Toast.makeText(getContext(), "No information added. Event not saved.", Toast.LENGTH_SHORT).show();
                }
                utils.fadeIn();
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
                getFragmentManager().popBackStackImmediate();
            }

        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");
                utils = new Utils(v.getContext());
                String input = mInput.getText().toString();
                String description = mDescription.getText().toString();
                if (input.isEmpty()){
                    Toast.makeText(getContext(),"No information added. Event not saved.", Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStackImmediate();
                }
                else{
                    UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(USER_ID,5000) {
                        @Override
                        public void onPostExecute() {
                            userEntry = getResult();
                            EventEntry eventEntry = new EventEntry();
                            eventEntry.setName(input);
                            eventEntry.setDescription(description);
                            eventEntry.setStart_time(new Timestamp(start));
                            eventEntry.setEnd_time(new Timestamp(end));
                            eventEntry.setUser_id_source(USER_ID);
                            userEntry.modifyEventOrTodo(true, true, true, eventEntry);
                            UserEntry.SetUserEntry setEvent = new UserEntry.SetUserEntry(userEntry, USER_ID, 5000) {
                                @Override
                                public void onPostExecute() {
                                    Toast.makeText(getContext(),"Event saved successfully.", Toast.LENGTH_SHORT).show();
                                    getFragmentManager().popBackStackImmediate();
//                            populateEventsList();
//                            textEventAdapter.notifyDataSetChanged();
//                        TextEventAdapter adapter = new TextEventAdapter(list, getContext());
//                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
//                        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
//                        mRecyclerView.setLayoutManager(linearLayoutManager);
//                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//                        mRecyclerView.setAdapter(adapter);
                                }
                            };
                            setEvent.start();
                        }
                    };
                    getUserEntry.start();
                }
                utils.fadeIn();
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
                                calendar.set(year,monthOfYear,dayOfMonth);
                                start = calendar.getTime();


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
                                calendar.set(year, monthOfYear, dayOfMonth);
                                end = calendar.getTime();
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(cldr.getTimeInMillis());
                picker.show();
            }
        });
        return root;
    }
}
