package com.example.beever.feature;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private static final String GROUP_ENTRIES = "groupEntries";
    private static final String GROUP_IDS = "groupIds";
    private static final String RELEVANT_EVENTS = "relevantEvents";
    private static final String USER_ENTRY = "userEntry";

    private UserEntry userEntry;
    private ArrayList<GroupEntry> groupEntries = new ArrayList<>();
    private ArrayList<String> groupIds = new ArrayList<>();
    private ArrayList<EventEntry> events = new ArrayList<>();

    private final Bundle newBundle = new Bundle();

    private static final String TAG = "CalendarFragment";
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    protected ArrayList<EventEntry> list = new ArrayList<>();
    protected Map<String,Object> map = new HashMap<>();
    private String USER_ID;
    private TextEventAdapter textEventAdapter;
    private Utils utils;
    private final Calendar calendar = Calendar.getInstance();
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    FloatingActionButton addEvent;
    View bottom_menu;
    ImageView noEventsImage;
    TextView noEventsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_calendar, container, false);

        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

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

        textEventAdapter = new TextEventAdapter(list,getContext(),USER_ID,getFragmentManager());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false);
        RecyclerView mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(textEventAdapter);
        noEventsImage = root.findViewById(R.id.no_events_image);
        noEventsText = root.findViewById(R.id.no_events_text);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            userEntry = bundle.getParcelable(USER_ENTRY);
            groupEntries = bundle.getParcelableArrayList(GROUP_ENTRIES);
            groupIds = bundle.getStringArrayList(GROUP_IDS);
            events = bundle.getParcelableArrayList(RELEVANT_EVENTS);

            populateEventsList();

        } else {
            UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(USER_ID, 5000) {
                @Override
                public void onPostExecute() {
                    userEntry = getResult();

                    UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(USER_ID, 5000) {
                        @Override
                        public void onPostExecute() {
                            userEntry = getResult();

                            for (Object o: userEntry.getGroups()) {
                                int full = getResult().getGroups().size();
                                GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry((String) o, 5000) {
                                    @Override
                                    public void onPostExecute() {
                                        if (isSuccessful()) {
                                            groupEntries.add(getResult());
                                            groupIds.add(getGroupId());
                                            if (groupEntries.size() == full) {
                                                populateEventsList();
                                            }
                                        }
                                    }
                                };
                                getGroupEntry.start();

                            }
                        }
                    };
                    getUserEntry.start();

                    UserEntry.GetUserRelevantEvents getUserRelevantEvents = new UserEntry.GetUserRelevantEvents(userEntry, 5000, true, false) {
                        @Override
                        public void onPostExecute() {
                            events = getResult();
                            populateEventsList();
                        }
                    };
                    getUserRelevantEvents.start();
                }
            };
            getUserEntry.start();
        }

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
                Fragment addEventFragment = new AddEventFragment();
                if (bundle != null) {
                    bundle.putInt("selectedDay", selectedDay);
                    bundle.putInt("selectedMonth", selectedMonth);
                    bundle.putInt("selectedYear", selectedYear);
                    addEventFragment.setArguments(bundle);
                } else {
                    newBundle.putInt("selectedDay", selectedDay);
                    newBundle.putInt("selectedMonth", selectedMonth);
                    newBundle.putInt("selectedYear", selectedYear);
                    addEventFragment.setArguments(newBundle);
                }
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

    public void populateEventsList(){

        Date date = calendar.getTime();
        Timestamp dateTimestamp = new Timestamp(date);
//                                Date startDate = new GregorianCalendar(selectedYear, selectedMonth,selectedDay).getTime();
        Timestamp startDate = new Timestamp(new Date((dateTimestamp.getSeconds())*1000));
        Timestamp endDate = new Timestamp(new Date((startDate.getSeconds() + 86400)*1000));
        Log.d(TAG, "populateEventsList: "+ USER_ID);
        Log.d(TAG, "START DATE " + startDate);
        Log.d(TAG, "END DATE " + endDate);
        ArrayList<EventEntry> eventEntries = new ArrayList<>();
        for (EventEntry e : events){
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

    static class TextEventAdapter extends RecyclerView.Adapter<CalendarFragment.TextEventAdapter.TextEventViewHolder>{

        private static final String TAG = "help";
        private final ArrayList<EventEntry> dbEvents;
        private final Context context;
        private final String USER_ID;
        private final FragmentManager fragmentManager;
        private static int moreThanDay;
        private final SimpleDateFormat sfDate = new SimpleDateFormat("dd MMM", Locale.getDefault());
        private final SimpleDateFormat sfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());


        public class TextEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView eventName;
            LinearLayout eventTiming;
            LinearLayout eventTiming_More;
            TextView startDate;
            TextView startTime;
            TextView endDate;
            TextView endTime;
            public TextEventViewHolder(@NonNull View itemView) {
                super(itemView);
                eventName = itemView.findViewById(R.id.event_title);
                eventTiming = itemView.findViewById(R.id.event);
                eventTiming_More = itemView.findViewById(R.id.event_more);
                if (moreThanDay == 1)  {
                    startDate = itemView.findViewById(R.id.start_date_more);
                    startTime = itemView.findViewById(R.id.start_time_more);
                    endDate = itemView.findViewById(R.id.end_date_more);
                    endTime = itemView.findViewById(R.id.end_time_more);
                } else {
                    startDate = itemView.findViewById(R.id.start_date);
                    startTime = itemView.findViewById(R.id.start_time);
                    endTime = itemView.findViewById(R.id.end_time);
                }

                itemView.setOnClickListener(this);

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int itemPos = getLayoutPosition();
                        EventEntry eventEntry = dbEvents.get(itemPos);
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Delete Event");
                        alertDialog.setMessage("Event Chosen: " + eventEntry.getName());
                        Log.d(TAG, "onLongClick: "+ USER_ID);
                        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (eventEntry.getGroup_id_source() != null){
                                    Toast.makeText(context,"Group event cannot be deleted",Toast.LENGTH_SHORT).show();
                                } else {
                                    dbEvents.remove(eventEntry);
                                    notifyDataSetChanged();
                                    UserEntry.UpdateUserEntry updateUserEntry = new UserEntry.UpdateUserEntry(USER_ID,
                                            UserEntry.UpdateUserEntry.FieldChange.USER_EVENTS_CURRENT_REMOVE, eventEntry, 5000) {
                                        @Override
                                        public void onPostExecute() {
                                            fragmentManager.beginTransaction().replace(R.id.fragment_container,new CalendarFragment()).commit();
                                            Toast.makeText(context,"Event successfully deleted",Toast.LENGTH_SHORT).show();
                                        }
                                    };
                                    updateUserEntry.start();

                                }
                            }
                        });
                        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        alertDialog.show();

                        return true;
                    }
                });
            }

            public TextView getEventName() {
                return eventName;
            }

            public LinearLayout getEventTiming() {
                return eventTiming;
            }

            public LinearLayout getEventTiming_More() {
                return eventTiming_More;
            }

            public TextView getStartDate() {
                return startDate;
            }

            public TextView getEndDate() {
                return endDate;
            }

            public TextView getStartTime() {
                return startTime;
            }

            public TextView getEndTime() {
                return endTime;
            }



            @Override
            public void onClick(View v) {
                int itemPos = getLayoutPosition();
                EventEntry eventEntry = dbEvents.get(itemPos);
//            fragmentManager.beginTransaction().replace(R.id.fragment_container,new EventViewFragment()).commit();

            }
        }

        public TextEventAdapter(ArrayList<EventEntry> data, Context context, String USER_ID, FragmentManager fragmentManager) {
            this.dbEvents = data;
            this.context = context;
            this.USER_ID = USER_ID;
            this.fragmentManager = fragmentManager;
            Log.d(TAG, "TextEventAdapter: " + data.toString());
//        this.mContext = context;
        }



        @Override
        public int getItemViewType(int position) {
            String start = sfDate.format(dbEvents.get(position).getStart_time().toDate());
            String end = sfDate.format(dbEvents.get(position).getEnd_time().toDate());
            if (start.equals(end)) {
                moreThanDay = 0;
            } else {
                moreThanDay = 1;
            }
            return moreThanDay;
        }

        @NonNull
        @Override
        public TextEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_card,parent,false);

            return new TextEventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarFragment.TextEventAdapter.TextEventViewHolder holder, int position) {
            String eventName = dbEvents.get(position).getName();
            String startTime = sfTime.format(dbEvents.get(position).getStart_time().toDate());
            String startDate = sfDate.format(dbEvents.get(position).getStart_time().toDate());
            String endTime = sfTime.format(dbEvents.get(position).getEnd_time().toDate());
            String endDate = sfDate.format(dbEvents.get(position).getEnd_time().toDate());

            Log.d("CHECK ADAPTER", "info includes - " +eventName+", " +startTime+", " +startDate+", " +endTime+", " +endDate);

            if (moreThanDay == 1) {
                holder.getEndDate().setText(endDate);
                holder.getEventTiming().setVisibility(View.GONE);
                holder.getEventTiming_More().setVisibility(View.VISIBLE);
            } else {
                holder.getEventTiming().setVisibility(View.VISIBLE);
                holder.getEventTiming_More().setVisibility(View.GONE);
            }
            holder.getEventName().setText(eventName);
            holder.getStartDate().setText(startDate);
            holder.getStartTime().setText(startTime);
            holder.getEndTime().setText(endTime);
        }

        @Override
        public int getItemCount() {
            return dbEvents.size();
        }

    }
}