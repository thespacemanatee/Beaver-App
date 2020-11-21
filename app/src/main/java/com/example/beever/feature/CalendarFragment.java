package com.example.beever.feature;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;
import android.util.Log;


public class CalendarFragment extends Fragment {
    FloatingActionButton addEvent;
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_calendar, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Calendar");

        addEvent = root.findViewById(R.id.addEvent);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment addEventFragment = new AddEventFragment();
                System.out.println("click");
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, addEventFragment).addToBackStack(null).commit();
            }
        });



//        final CollapsibleCalendar collapsibleCalendar = root.findViewById(R.id.calendarView);
//        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
//            @Override
//            public void onDaySelect() {
//                Day day = collapsibleCalendar.getSelectedDay();
//                Log.i(getClass().getName(), "Selected Day: "
//                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
//            }
//
//            @Override
//            public void onItemClick(View view) {
//
//            }
//
//            @Override
//            public void onDataUpdate() {
//
//            }
//
//            @Override
//            public void onMonthChange() {
//
//            }
//
//            @Override
//            public void onWeekChange(int i) {
//
//            }
//
//            @Override
//            public void onDayChanged() {
//
//            }
//
//            @Override
//            public void onClickListener() {
//
//            }
//        });


        return root;
    }


}