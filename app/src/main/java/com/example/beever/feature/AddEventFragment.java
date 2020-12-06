package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

public class AddEventFragment extends Fragment {

    private static final String TAG = "AddEventFragment";
    private static final String GROUP_ENTRIES = "groupEntries";
    private static final String GROUP_IDS = "groupIds";
    private static final String RELEVANT_EVENTS = "relevantEvents";
    private static final String DASH_GROUP_ENTRIES = "dashGroupEntries";
    private static final String DASH_GROUP_IDS = "dashGroupIds";
    private static final String USER_ENTRY = "userEntry";

    private UserEntry userEntry;
    private ArrayList<GroupEntry> groupEntries = new ArrayList<>();
    private ArrayList<String> groupIds = new ArrayList<>();
    private ArrayList<EventEntry> events = new ArrayList<>();

    private EditText mInput,mDescription;
    private LinearLayout eventStart, eventEnd, addEventButtons;
    private Button startDate, endDate, startTime, endTime;
    private Button cancel, save;
    private Calendar start, end;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String USER_ID;
    private Utils utils;
    private int selectedDay, selectedMonth, selectedYear;
    private int startMinute, startHour, startDay, startMonth, startYear;
    private int endMinute, endHour, endDay, endMonth, endYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addevent, container, false);
        mInput = root.findViewById(R.id.eventTitle);
        mDescription = root.findViewById(R.id.description);
        eventStart = root.findViewById(R.id.startEvent);
        startDate = eventStart.findViewById(R.id.start_date);
        startTime = eventStart.findViewById(R.id.start_time);
        eventEnd = root.findViewById(R.id.endEvent);
        endDate = eventEnd.findViewById(R.id.end_date);
        endTime = eventEnd.findViewById(R.id.end_time);
        addEventButtons = root.findViewById(R.id.add_event_buttons);
        cancel = addEventButtons.findViewById(R.id.cancel_button);
        save = addEventButtons.findViewById(R.id.save_button);

        Bundle bundle = this.getArguments();
        selectedDay = bundle.getInt("selectedDay");
        selectedMonth = bundle.getInt("selectedMonth");
        selectedYear = bundle.getInt("selectedYear");
//        userEntry = bundle.getParcelable(USER_ENTRY);
//        groupEntries = bundle.getParcelableArrayList(GROUP_ENTRIES);
//        groupIds = bundle.getStringArrayList(GROUP_IDS);
//        events = bundle.getParcelableArrayList(RELEVANT_EVENTS);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        USER_ID = firebaseUser.getUid();

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        startYear = selectedYear;
        startMonth = selectedMonth;
        startDay = selectedDay;
        startHour = 0;
        startMinute = 0;

        endYear = selectedYear;
        endMonth = selectedMonth;
        endDay = selectedDay;
        endHour = 0;
        endMinute = 0;

        start = Calendar.getInstance();
        start.set(startYear,startMonth,startDay,startHour,startMinute,0);
        end = Calendar.getInstance();
        end.set(endYear,endMonth,endDay,endHour,endMinute,0);

        startDate.setText(startDay + "/" + (startMonth + 1) + "/" + startYear);
        endDate.setText(endDay + "/" + (endMonth + 1) + "/" + endYear);
        startTime.setText(startHour + (startMinute==0? ":00" : Integer.toString(startMinute)));
        endTime.setText(endHour + (endMinute==0? ":00" : Integer.toString(endMinute)));



//        startDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
//        startTime.setText(calendar.getTime().toString().substring(11,19));
//        endDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
//        endTime.setText(calendar.getTime().toString().substring(11,19));


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
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
                if (input.isEmpty()){
                    Toast.makeText(getContext(),"Please enter event name.", Toast.LENGTH_SHORT).show();
                    //getFragmentManager().popBackStackImmediate();
                }
                else if (start.compareTo(end)>=0){
                    Toast.makeText(getContext(),"Please enter valid start/end times.", Toast.LENGTH_SHORT).show();
                }
                else{
                    UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(USER_ID,5000) {
                        @Override
                        public void onPostExecute() {
                            userEntry = getResult();
                            EventEntry eventEntry = new EventEntry();
                            eventEntry.setName(input);
                            eventEntry.setDescription(description);
                            Log.d("start",start.toString());
                            Log.d("end",end.toString());
                            eventEntry.setStart_time(new Timestamp(start.getTime()));
                            eventEntry.setEnd_time(new Timestamp(end.getTime()));
                            eventEntry.setUser_id_source(USER_ID);
                            userEntry.modifyEventOrTodo(true, true, true, eventEntry);
                            UserEntry.SetUserEntry setEvent = new UserEntry.SetUserEntry(userEntry, USER_ID, 5000) {
                                @Override
                                public void onPostExecute() {
                                    Toast.makeText(getContext(),"Event saved successfully.", Toast.LENGTH_SHORT).show();
                                    CalendarFragment calendarFragment = new CalendarFragment();
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,calendarFragment).commit();
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

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                startDay = dayOfMonth;
                                startMonth = monthOfYear;
                                startYear = year;
                                start.set(startYear,startMonth,startDay,startHour,startMinute,0);
//                                calendar.set(year,monthOfYear,dayOfMonth);
//                                start = calendar.getTime();
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(cldr.getTimeInMillis());
                picker.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                endDay = dayOfMonth;
                                endMonth = monthOfYear;
                                endYear = year;
                                end.set(endYear,endMonth,endDay,endHour,endMinute,0);
//                                calendar.set(year, monthOfYear, dayOfMonth);
//                                end = calendar.getTime();
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(cldr.getTimeInMillis());
                picker.show();
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timeSelectFragment = new AddEventFragment.TimeSelectFragment(AddEventFragment.this, true);
                timeSelectFragment.show(getFragmentManager(),"TimeSelectFragment");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timeSelectFragment = new AddEventFragment.TimeSelectFragment(AddEventFragment.this, false);
                timeSelectFragment.show(getFragmentManager(),"TimeSelectFragment");
            }
        });

        return root;
    }

    public static class TimeSelectFragment extends DialogFragment{

        private AddEventFragment addEventFragment = null;
        private NumberPicker add_event_select_hour = null;
        private NumberPicker add_event_select_minute = null;
        private boolean start;

        private final String[] hourPicks = new String[] {"0","1","2","3","4","5","6","7","8","9","10","11","12",
                "13","14","15","16","17","18","19","20","21","22","23"};
        private final String[] minutePicks = new String[] {"00","15","30","45"};

        public TimeSelectFragment(AddEventFragment addEventFragment, Boolean start){
            super();
            this.addEventFragment = addEventFragment;
            this.start = start;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialog = inflater.inflate(R.layout.fragment_gap_finder_time_select,null);

            add_event_select_hour = dialog.findViewById(R.id.gap_finder_select_hour);
            add_event_select_minute = dialog.findViewById(R.id.gap_finder_select_minute);

            add_event_select_hour.setMaxValue(hourPicks.length-1);
            add_event_select_hour.setMinValue(0);
            add_event_select_hour.setDisplayedValues(hourPicks);

            add_event_select_minute.setMaxValue(minutePicks.length-1);
            add_event_select_minute.setMinValue(0);
            add_event_select_minute.setDisplayedValues(minutePicks);

            builder.setView(dialog).setTitle("Select time")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    gapFinderFragment.setHourMinute(hourPicks[gap_finder_select_hour.getValue()],
//                            minutePicks[gap_finder_select_minute.getValue()]);
                    if (start) {
                        addEventFragment.startHour = Integer.parseInt(hourPicks[add_event_select_hour.getValue()]);
                        addEventFragment.startMinute = Integer.parseInt(minutePicks[add_event_select_minute.getValue()]);
                        if (addEventFragment.startMinute == 0){
                            addEventFragment.startTime.setText(addEventFragment.startHour + ":00");
                        } else {
                            addEventFragment.startTime.setText(addEventFragment.startHour + ":" + addEventFragment.startMinute);
                        }
                        addEventFragment.start.set(addEventFragment.startYear,addEventFragment.startMonth,addEventFragment.startDay,addEventFragment.startHour,addEventFragment.startMinute, 0);
//                        addEventFragment.calendar.set(addEventFragment.startYear,addEventFragment.startMonth,addEventFragment.startDay);
//                        addEventFragment.start = new GregorianCalendar(addEventFragment.startYear,addEventFragment.startMonth,addEventFragment.startDay,addEventFragment.startHour,addEventFragment.startMinute, 0).getTime();
                        Log.d(TAG, "onClick: "+ addEventFragment.start.toString());
                    } else {
                        addEventFragment.endHour = Integer.parseInt(hourPicks[add_event_select_hour.getValue()]);
                        addEventFragment.endMinute = Integer.parseInt(minutePicks[add_event_select_minute.getValue()]);
                        if (addEventFragment.endMinute == 0){
                            addEventFragment.endTime.setText(addEventFragment.endHour + ":00");
                        } else {
                            addEventFragment.endTime.setText(addEventFragment.endHour + ":" + addEventFragment.endMinute);
                        }
                        addEventFragment.end.set(addEventFragment.endYear, addEventFragment.endMonth, addEventFragment.endDay, addEventFragment.endHour, addEventFragment.endMinute, 0);
//                        addEventFragment.calendar.set(addEventFragment.endYear, addEventFragment.endMonth, addEventFragment.endDay);
//                        addEventFragment.end = new GregorianCalendar(addEventFragment.endYear, addEventFragment.endMonth, addEventFragment.endDay, addEventFragment.endHour, addEventFragment.endMinute, 0).getTime();
                        Log.d(TAG, "onClick: " + addEventFragment.end.toString());
                    }

                }
            });
            return builder.create();
        }
    }
}
