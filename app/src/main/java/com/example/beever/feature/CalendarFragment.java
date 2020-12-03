package com.example.beever.feature;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    protected ArrayList<EventEntry> list = new ArrayList<>();
    protected Map<String,Object> map = new HashMap<>();
    private final String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private String USER_ID;
    private UserEntry userEntry;
    private TextEventAdapter textEventAdapter;
    private RecyclerView mRecyclerView;
    private Utils utils;
    private Calendar calendar = Calendar.getInstance();
    private int selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
    private int selectedMonth = calendar.get(Calendar.MONTH);
    private int selectedYear = calendar.get(Calendar.YEAR);
    FloatingActionButton addEvent;
    Bundle bundle = new Bundle();
    View bottom_menu;
    ImageView noEventsImage;
    TextView noEventsText;

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

        //Fade in Nav Bar
        bottom_menu = getActivity().findViewById(R.id.bottom_menu);
        if (bottom_menu.getVisibility() == View.GONE) {
            Utils utils = new Utils(getContext());
            utils.fadeIn();
        }

        textEventAdapter = new TextEventAdapter(list,getContext(),USER_ID);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(textEventAdapter);
        noEventsImage = root.findViewById(R.id.no_events_image);
        noEventsText = root.findViewById(R.id.no_events_text);

        calendar.set(selectedYear,selectedMonth,selectedDay,0,0,0);
        populateEventsList();

        addEvent = root.findViewById(R.id.addEvent);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog");
//                AddEventFragment dialog = new AddEventFragment();
//                dialog.show(getFragmentManager(),"AddEventDialog");
                utils = new Utils(v.getContext());
                utils.fadeOut();
                bundle.putInt("selectedDay",selectedDay);
                bundle.putInt("selectedMonth",selectedMonth);
                bundle.putInt("selectedYear",selectedYear);
                Fragment addEventFragment = new AddEventFragment();
                addEventFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, addEventFragment).addToBackStack(null).commit();
//                customDialog("New Event", "Edit new event", "cancel", "save");
            }
        });

        CalendarView calendarView = root.findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDay = dayOfMonth;
                selectedMonth = month;
                selectedYear = year;
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                populateEventsList();
            }
        });

        return root;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        //Fade in Nav Bar
        bottom_menu = getActivity().findViewById(R.id.bottom_menu);
        if (bottom_menu.getVisibility() == View.GONE) {
            Utils utils = new Utils(getContext());
            utils.fadeIn();
        }
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
                                Date date = calendar.getTime();
                                Timestamp dateTimestamp = new Timestamp(date);
//                                Date startDate = new GregorianCalendar(selectedYear, selectedMonth,selectedDay).getTime();
                                Timestamp startDate = new Timestamp(new Date((dateTimestamp.getSeconds())*1000));
                                Timestamp endDate = new Timestamp(new Date((startDate.getSeconds() + 86400)*1000));
                                Log.d(TAG, "START DATE " + startDate);
                                Log.d(TAG, "END DATE " + endDate);
                                ArrayList<EventEntry> eventEntries = new ArrayList<>();
                                for (EventEntry e : getResult()){
                                    Log.d(TAG, "onPostExecute: " + e.getStart_time());
                                    if (!(e.getStart_time().getSeconds() >= endDate.getSeconds() || e.getEnd_time().getSeconds() < startDate.getSeconds())){
                                        eventEntries.add(e);
                                    }
                                }
                                list.clear();
                                list.addAll(eventEntries);

                                if (list.size() > 0) {
                                    noEventsImage.setVisibility(View.GONE);
                                    noEventsText.setVisibility(View.GONE);
                                }

                                if (list.size() == 0) {
                                    noEventsImage.setVisibility(View.VISIBLE);
                                    noEventsText.setVisibility(View.VISIBLE);
                                }
//                                textEventAdapter = new TextEventAdapter(list);
//                                mRecyclerView.setAdapter(textEventAdapter);
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