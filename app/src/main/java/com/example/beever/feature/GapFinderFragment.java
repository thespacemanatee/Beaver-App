package com.example.beever.feature;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class GapFinderFragment extends Fragment implements AdapterView.OnItemSelectedListener, GapAdapter.OnTimestampListener {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private RecyclerView mRecyclerView;
    private String groupName;
    private String groupID;
    private MaterialButton preferredDate, preferredTime;
    private CircularProgressButton searchBtn;
    private TextView currentTime, result;
    private TextInputLayout eventName, eventDesc;
    private Spinner spin;
    private GroupEntry groupEntry;
    private static final Calendar combinedCal = Calendar.getInstance();
    private static final Calendar chosenDay = Calendar.getInstance();
    private ArrayList<Timestamp> timestamps = new ArrayList<>();
    private ArrayList<Timestamp> timestampsEnd = new ArrayList<>();
    private ArrayList<Timestamp> startTimes = new ArrayList<>();
    private ArrayList<Timestamp> endTimes = new ArrayList<>();
    private ArrayList<EventEntry> groupEntries = new ArrayList<>();
    private Integer[] durations = new Integer[10];
    private int CHOSEN_DURATION;
    private GapAdapter adapter;
    private static int queryDay;
    private static int queryMonth;
    private static int queryYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gap_finder, container, false);

        Bundle bundle = this.getArguments();

//        groupImage = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupId");

        mRecyclerView = rootView.findViewById(R.id.gap_finder_recycler);
        preferredDate = rootView.findViewById(R.id.preferred_date);
        preferredTime = rootView.findViewById(R.id.preferred_time);
        searchBtn = rootView.findViewById(R.id.search_button);
        currentTime = rootView.findViewById(R.id.current_preferred_text);
        result = rootView.findViewById(R.id.gap_result);
        spin = rootView.findViewById(R.id.spinner);
        eventName = rootView.findViewById(R.id.event_name);
        eventDesc = rootView.findViewById(R.id.event_description);

//        String pattern = "HH:mm";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        try {
//            Date date = simpleDateFormat.parse("00:00");
//            chosenDay.setTime(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        spin.setOnItemSelectedListener(this);

        mRecyclerView = rootView.findViewById(R.id.gap_finder_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new GapAdapter(timestamps, this);
        mRecyclerView.setAdapter(adapter);

        for (int i = 0; i < 10; i++) {
            durations[i] = (i+1)*15;
        }

        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, durations);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        getListOfEvents();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtn.startAnimation();
                currentTime.setText("Current preferred time: " + combinedCal.getTime().toString().substring(0, 16));
                Timestamp timestamp = new Timestamp(combinedCal.getTime());
                Log.d("TIME SELECTED", combinedCal.getTime().toString());
                Log.d("TIME SELECTED", timestamp.toString());
                gapFinder(timestamp);
            }
        });

        preferredTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        preferredDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        return rootView;
    }

    private void gapFinder(Timestamp timestamp) {
        for (int i = 0; i < startTimes.size(); i++) {
            if (timestamp.getSeconds() > startTimes.get(i).getSeconds()
                    && timestamp.getSeconds() < endTimes.get(i).getSeconds()
                    || timestamp.getSeconds() < startTimes.get(i).getSeconds()
                    && (timestamp.getSeconds() + CHOSEN_DURATION*60) > startTimes.get(i).getSeconds()
                    && (timestamp.getSeconds() + CHOSEN_DURATION*60) < endTimes.get(i).getSeconds()) {

                timestamps.clear();
                result.setText("Result: Timeslot is unavailable!\nHow about: ");
                findAlternativeTimings();
                searchBtn.revertAnimation();
                break;
            } else {
                timestamps.clear();
                result.setText("Result: " + combinedCal.getTime().toString().substring(0, 16) + " is available!");
                timestamps.add(timestamp);
                timestampsEnd.add(new Timestamp(new Date((timestamp.getSeconds() + CHOSEN_DURATION*60)*1000)));
                adapter.notifyDataSetChanged();
            }
        }
        searchBtn.revertAnimation();
    }

    private void findAlternativeTimings() {
        timestamps.clear();
        timestampsEnd.clear();
        GapFinderAlgorithm gapFinder = new GapFinderAlgorithm(groupID,10000,
                queryYear, queryMonth, queryDay, CHOSEN_DURATION){
            public void onPostExecute(){
                if (isSuccessful()){
                    ArrayList<ArrayList<Timestamp>> timingBlocks = getResult();
                    for (ArrayList<Timestamp> timeList: timingBlocks) {
                        timestamps.add(timeList.get(0));
                        timestampsEnd.add(timeList.get(1));
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("FINAL RESULT", timestamps.toString());
                }
            }
        };
        gapFinder.getGaps();
    }

    private void getListOfEvents() {
        GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupID, 5000) {

            @Override
            public void onPostExecute() {
                groupEntry = getResult();
                GroupEntry.GetGroupRelevantEvents groupRelevantEvents = new GroupEntry.GetGroupRelevantEvents(groupEntry, 5000) {

                    @Override
                    public void onPostExecute() {
                        groupEntries = getResult();
                        Log.d("RELEVANT EVENTS", groupEntries.toString());
                        getStartAndEndTime();
                    }
                };
                groupRelevantEvents.start();
            }
        };
        getGroupEntry.start();
    }

    private void getStartAndEndTime() {
        for (EventEntry entry: groupEntries) {
            startTimes.add(entry.getStart_time());
            endTimes.add(entry.getEnd_time());
        }
        Log.d("START TIMES", startTimes.toString());
        Log.d("END TIMES", endTimes.toString());
    }


    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CHOSEN_DURATION = durations[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onTimestampClick(int position) {
        String name = eventName.getEditText().getText().toString();
        String description = eventDesc.getEditText().getText().toString();
        if (!name.isEmpty()) {
            name = eventName.getEditText().getText().toString();
            eventName.setError(null);
            eventName.setErrorEnabled(false);
        } else {
            eventName.setError("Please enter an event name!");
        }
        if (!description.isEmpty()) {
            description = eventDesc.getEditText().getText().toString();
            eventDesc.setError(null);
            eventDesc.setErrorEnabled(false);
        } else {
            eventDesc.setError("Please enter an event name!");
        }
        if (!name.isEmpty() && !description.isEmpty()) {
            EventEntry eventEntry = new EventEntry();
            eventEntry.setName(name);
            eventEntry.setDescription(description);
            eventEntry.setGroup_id_source(groupID);
            eventEntry.setStart_time(timestamps.get(position));
            eventEntry.setEnd_time(timestampsEnd.get(position));
            GroupEntry.SetGroupEntry setEvent = new GroupEntry.SetGroupEntry(groupEntry, groupID, 5000) {
                @Override
                public void onPostExecute() {
                    groupEntry.modifyEventOrTodo(true, true, true, eventEntry);
                    Log.d("GAP RESULT", "onPostExecute: " + timestamps.get(position).toString() + timestampsEnd.get(position).toString());
                }
            };
            setEvent.start();
        }


    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            combinedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            combinedCal.set(Calendar.MINUTE, minute);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            combinedCal.set(year, month, day);
            chosenDay.set(year, month, day);
            queryDay = day;
            queryMonth = month;
            queryYear = year;
        }
    }
}