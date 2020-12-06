package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class GapFinderFragment extends Fragment implements AdapterView.OnItemSelectedListener, GapAdapter.OnTimestampListener {

    private final int DURATION_BLOCK_UPPER_LIMIT = 10;
    private String groupID;
    private CircularProgressButton searchBtn;
    private TextView currentTime, result;
    private TextInputLayout eventName, eventDesc;
    private GroupEntry groupEntry;
    private final ArrayList<Timestamp> timestamps = new ArrayList<>();
    private final ArrayList<Timestamp> timestampsEnd = new ArrayList<>();
    private final Integer[] durations = new Integer[DURATION_BLOCK_UPPER_LIMIT];
    private int chosenDuration;
    private GapAdapter adapter;
    private int queryYear;
    private int queryMonth;
    private int queryDay;
    private int queryHour;
    private int queryMinute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gap_finder, container, false);

        Bundle bundle = this.getArguments();

//        groupImage = bundle.getString("imageUri");
        String groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupId");

        RecyclerView mRecyclerView = rootView.findViewById(R.id.gap_finder_recycler);
        MaterialButton preferredDate = rootView.findViewById(R.id.preferred_date);
        MaterialButton preferredTime = rootView.findViewById(R.id.preferred_time);
        searchBtn = rootView.findViewById(R.id.search_button);
        currentTime = rootView.findViewById(R.id.current_preferred_text);
        result = rootView.findViewById(R.id.gap_result);
        Spinner spin = rootView.findViewById(R.id.spinner);
        eventName = rootView.findViewById(R.id.event_name);
        eventDesc = rootView.findViewById(R.id.event_description);

        spin.setOnItemSelectedListener(this);

        mRecyclerView = rootView.findViewById(R.id.gap_finder_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new GapAdapter(timestamps, this);
        mRecyclerView.setAdapter(adapter);

        for (int i = 0; i < DURATION_BLOCK_UPPER_LIMIT; i++) {
            durations[i] = (i+1)*15;
        }

        Calendar c = Calendar.getInstance();
        queryYear = c.get(Calendar.YEAR);
        queryMonth = c.get(Calendar.MONTH);
        queryDay = c.get(Calendar.DAY_OF_MONTH);
        queryHour = (c.get(Calendar.HOUR_OF_DAY)>=1 && c.get(Calendar.HOUR_OF_DAY)<8)? 8 : c.get(Calendar.HOUR_OF_DAY);
        queryMinute = (c.get(Calendar.MINUTE)/15) *15;
        c.set(queryYear,queryMonth,queryDay,queryHour,queryMinute,0);
        setPreferredTimeIndicator(c);

        ArrayAdapter<Integer> aa = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, durations);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        GroupEntry.GetGroupEntry groupEntryGetter = new GroupEntry.GetGroupEntry(groupID,5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) groupEntry = getResult();
            }
        };
        groupEntryGetter.start();

        //getListOfEvents();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO toggling
                searchBtn.startAnimation();
                Calendar c = Calendar.getInstance();
                c.set(queryYear, queryMonth, queryDay, queryHour, queryMinute,0);
                setPreferredTimeIndicator(c);
                    //Log.d("TIME SELECTED", combinedCal.getTime().toString());
                    //Log.d("TIME SELECTED", timestamp.toString());
                gapFinder();
            }
        });

        preferredTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showTimePickerDialog(v);
                DialogFragment timeSelectFragment = new TimeSelectFragment(GapFinderFragment.this);
                timeSelectFragment.show(getFragmentManager(),"TimeSelectFragment");
            }
        });

        preferredDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        return rootView;
    }

    private void gapFinder() {
        Calendar c = Calendar.getInstance();
        c.set(queryYear,queryMonth,queryDay,queryHour,queryMinute,0);
        ///Log.d("test",getCalendarString(c));
        GapFinderAlgorithm gapFinder = new GapFinderAlgorithm(groupID,10000,queryYear,queryMonth,queryDay,queryHour,queryMinute,chosenDuration) {
            @Override
            public void onPostExecute() {
                if (!isSuccessful()){
                    Toast toast = Toast.makeText(getContext(), "There was an error. Please try again.", Toast.LENGTH_SHORT);
                    toast.show();
                    searchBtn.revertAnimation();
                    return;
                }
                if (isAvailable()){
                    Calendar c = Calendar.getInstance();
                    c.set(queryYear,queryMonth,queryDay,queryHour,queryMinute,0);
                    result.setText("Result: " + getCalendarString(c) + " is available. Please select timeslot to confirm.");
                    timestamps.clear();
                    timestampsEnd.clear();
                    timestamps.add(getTargetStartTimestamp());
                    timestampsEnd.add(getTargetEndTimestamp());
                    adapter.notifyDataSetChanged();
                    searchBtn.revertAnimation();
                    return;
                }
                if (getResult().size()==0){
                    result.setText("Result: Timeslot is unavailable, and no other timings available. Try another day?");
                    timestamps.clear();
                    timestampsEnd.clear();
                    adapter.notifyDataSetChanged();
                    searchBtn.revertAnimation();
                    return;
                }
                if (isIllegalTiming()) result.setText("Result: Timeslot is not allowed! How about (select to confirm): ");
                else result.setText("Result: Timeslot is unavailable. How about (select to confirm): ");
                timestamps.clear();
                timestampsEnd.clear();
                for (ArrayList<Timestamp> t:getResult()){
                    timestamps.add(t.get(0));
                    timestampsEnd.add(t.get(1));
                }
                adapter.notifyDataSetChanged();
                searchBtn.revertAnimation();
            }
        };
        gapFinder.getGaps();
    }

    public void setHourMinute(String hour,String minute){
        queryHour = Integer.parseInt(hour);
        queryMinute = Integer.parseInt(minute);
        Calendar c = Calendar.getInstance();
        c.set(queryYear,queryMonth,queryDay,queryHour,queryMinute,0);
        setPreferredTimeIndicator(c);
    }

    public void setYearMonthDay(int year,int month,int day){
        queryYear = year;
        queryMonth = month;
        queryDay = day;
        Calendar c = Calendar.getInstance();
        c.set(queryYear,queryMonth,queryDay,queryHour,queryMinute,0);
        setPreferredTimeIndicator(c);
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment(GapFinderFragment.this);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chosenDuration = durations[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onTimestampClick(int position) {
        final String name = eventName.getEditText().getText().toString();
        final String description = eventDesc.getEditText().getText().toString();
        if (!name.isEmpty()) {
            eventName.setError(null);
            eventName.setErrorEnabled(false);
        } else {
            eventName.setError("Please enter an event name!");
        }
        if (!description.isEmpty()) {
            eventDesc.setError(null);
            eventDesc.setErrorEnabled(false);
        } else {
            eventDesc.setError("Please enter an event name!");
        }
        if (!name.isEmpty() && !description.isEmpty()) {
            GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupID,5000){
                @Override
                public void onPostExecute() {
                    if (!isSuccessful()){
                        Toast toast = Toast.makeText(getContext(), "There was an error. Please try again.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    groupEntry = getResult();
                    EventEntry eventEntry = new EventEntry();
                    eventEntry.setName(name);
                    eventEntry.setDescription(description);
                    eventEntry.setGroup_id_source(groupID);
                    eventEntry.setStart_time(timestamps.get(position));
                    eventEntry.setEnd_time(timestampsEnd.get(position));
                    groupEntry.modifyEventOrTodo(true, true, true, eventEntry);
                    GroupEntry.SetGroupEntry setEvent = new GroupEntry.SetGroupEntry(groupEntry, groupID, 5000) {
                        @Override
                        public void onPostExecute() {
                            if (!isSuccessful()) {
                                Toast toast = Toast.makeText(getContext(), "There was an error. Please try again.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(getContext(), "Event added!", Toast.LENGTH_SHORT);
                                toast.show();
                                timestamps.clear();
                                timestampsEnd.clear();
                                eventName.getEditText().setText("");
                                eventDesc.getEditText().setText("");
                                adapter.notifyDataSetChanged();
                                result.setText("");
                            }
                        }
                    };
                    setEvent.start();
                }
            };
            getGroupEntry.start();
        }
    }

    private String getCalendarString(Calendar c){
        return c.getTime().toString().substring(0, 16);
    }

    private void setPreferredTimeIndicator(Calendar c){
        currentTime.setText("Current preferred time: " + getCalendarString(c));
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private GapFinderFragment gapFinderFragment = null;

        public DatePickerFragment(GapFinderFragment gapFinderFragment){
            super();
            this.gapFinderFragment = gapFinderFragment;
        }

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
            gapFinderFragment.setYearMonthDay(year,month,day);
        }
    }

    public static class TimeSelectFragment extends DialogFragment{

        private final GapFinderFragment gapFinderFragment;
        private NumberPicker gap_finder_select_hour = null;
        private NumberPicker gap_finder_select_minute = null;
        
        private final String[] hourPicks = new String[] {"0","8","9","10","11","12","13","14",
        "15","16","17","18","19","20","21","22","23"};
        private final String[] minutePicks = new String[] {"00","15","30","45"};

        public TimeSelectFragment(GapFinderFragment gapFinderFragment){
            super();
            this.gapFinderFragment = gapFinderFragment;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialog = inflater.inflate(R.layout.fragment_gap_finder_time_select,null);

            gap_finder_select_hour = dialog.findViewById(R.id.gap_finder_select_hour);
            gap_finder_select_minute = dialog.findViewById(R.id.gap_finder_select_minute);

            gap_finder_select_hour.setMaxValue(hourPicks.length-1);
            gap_finder_select_hour.setMinValue(0);
            gap_finder_select_hour.setDisplayedValues(hourPicks);

            gap_finder_select_minute.setMaxValue(minutePicks.length-1);
            gap_finder_select_minute.setMinValue(0);
            gap_finder_select_minute.setDisplayedValues(minutePicks);

            builder.setView(dialog).setTitle("Select time")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                        public void onClick(DialogInterface dialog, int which) {
                            gapFinderFragment.setHourMinute(hourPicks[gap_finder_select_hour.getValue()],
                                    minutePicks[gap_finder_select_minute.getValue()]);
                        }
                    });
            return builder.create();
        }
    }
}