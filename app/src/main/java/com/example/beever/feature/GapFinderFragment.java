package com.example.beever.feature;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class GapFinderFragment extends Fragment implements Populatable{

    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private RecyclerView mRecyclerView;
    private String groupName;
    private String groupID;
    private MaterialButton preferredDate, preferredTime;
    private CircularProgressButton searchBtn;
    private TextView currentTime;
    private static Calendar combinedCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

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

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtn.startAnimation();
                currentTime.setText("Current preferred time: " + combinedCal.getTime().toString().substring(0, 16));
                Timestamp timestamp = new Timestamp(combinedCal.getTime());
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

    @Override
    public void populateRecyclerView() {

    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    static class GapAdapter extends RecyclerView.Adapter<GapAdapter.ViewHolder> {
        private ArrayList<Timestamp> timestamps;
        public static class ViewHolder extends RecyclerView.ViewHolder{
            private TextView timestampTitle;
            private TextView timestampContent;


            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                timestampTitle = itemView.findViewById(R.id.timestamp_text_title);
                timestampContent = itemView.findViewById(R.id.timestamp_text_content);
            }

            public TextView getTimestampTitle() {
                return timestampTitle;
            }

            public TextView getTimestampContent() {
                return timestampContent;
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         *
         * @param timestamps ArrayList<Timestamp> containing the data to populate views to be used
         * by RecyclerView.
         */
        public GapAdapter(ArrayList<Timestamp> timestamps) {

        }

        @NonNull
        @Override
        public GapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gap_finder_cells, parent, false);

            return new GapAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GapAdapter.ViewHolder holder, int position) {
            holder.getTimestampTitle().setText(timestamps.get(position).toString());
            holder.getTimestampContent().setText(timestamps.get(position).toString());

        }

        @Override
        public int getItemCount() {
            return 0;
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
        }
    }
}