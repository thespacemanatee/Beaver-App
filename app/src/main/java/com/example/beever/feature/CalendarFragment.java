package com.example.beever.feature;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    FloatingActionButton addEvent;
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    ArrayList<Events> list = new ArrayList<>();
    private final String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_calendar, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Calendar");
        list.clear();
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));
        list.add(new Events(Events.TEXT_TYPE,0,"Hello"));

        TextEventAdapter adapter = new TextEventAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);

        addEvent = root.findViewById(R.id.addEvent);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog");
//                AddEventFragment dialog = new AddEventFragment();
//                dialog.show(getFragmentManager(),"AddEventDialog");
                Fragment addEventFragment = new AddEventFragment();
                System.out.println("click");
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, addEventFragment).addToBackStack(null).commit();
//                customDialog("New Event", "Edit new event", "cancel", "save");
            }
        });

        CalendarView calendarView = root.findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                list.clear();
                list.add(new Events(Events.TEXT_TYPE,0,"Event for " + dayOfMonth + " " + months[month] + " " + year));
                TextEventAdapter adapter = new TextEventAdapter(list, getContext());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
                RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
                mRecyclerView.setLayoutManager(linearLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(adapter);
            }
        });

        return root;
    }

    /**
     * Custom dialog that helps to edit the events
     * @param title
     * @param message
     * @param cancelMethod
     * @param okMethod
     */

    public void customDialog(String title, String message, final String cancelMethod, final String okMethod){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }


}