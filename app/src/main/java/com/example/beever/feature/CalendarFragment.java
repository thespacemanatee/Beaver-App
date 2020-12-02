package com.example.beever.feature;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    FloatingActionButton addEvent;
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    protected ArrayList<EventEntry> list = new ArrayList<>();
    protected Map<String,Object> map = new HashMap<>();
    private final String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private String USER_ID;
    private UserEntry userEntry;
    private TextEventAdapter textEventAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_calendar, container, false);

        /**
         * Get user ID from firebase authentication
         */
        FirebaseUser fUser = fAuth.getCurrentUser();
        USER_ID = fUser.getUid();

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Calendar");
        list.clear();

        textEventAdapter = new TextEventAdapter(list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(textEventAdapter);

        populateEventsList();

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
                EventEntry eventEntry = new EventEntry();
                eventEntry.setName("Event for " + dayOfMonth + " " + months[month] + " " + year);
                eventEntry.setStart_time(new Timestamp(new Date()));
                eventEntry.setEnd_time(new Timestamp(new Date()));
                eventEntry.setUser_id_source(USER_ID);
                userEntry.modifyEventOrTodo(true, true, true, eventEntry);
                UserEntry.SetUserEntry setEvent = new UserEntry.SetUserEntry(userEntry, USER_ID, 5000) {
                    @Override
                    public void onPostExecute() {
                        populateEventsList();
                        textEventAdapter.notifyDataSetChanged();
//                        TextEventAdapter adapter = new TextEventAdapter(list, getContext());
//                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
//                        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
//                        mRecyclerView.setLayoutManager(linearLayoutManager);
//                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//                        mRecyclerView.setAdapter(adapter);
                    }
                };
//                setEvent.start();

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

    public void populateEventsList(){
        UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(USER_ID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    userEntry = getResult();
                    UserEntry.GetUserRelevantEvents getUserRelevantEvents = new UserEntry.GetUserRelevantEvents(userEntry, 5000, true, false) {
                        @Override
                        public void onPostExecute() {
                            if (isSuccessful()) {
                                list = getResult();
                                textEventAdapter = new TextEventAdapter(list);
                                mRecyclerView.setAdapter(textEventAdapter);
                                textEventAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onPostExecute: " + list.toString());
                            }
                        }
                    };

                    getUserRelevantEvents.start();
                }
            }
        };
        getUserEntry.start();
    }
}