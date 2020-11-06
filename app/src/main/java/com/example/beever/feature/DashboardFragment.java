package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Calendar;

public class DashboardFragment extends Fragment {

    int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private SharedPreferences mSharedPref;
    TextView greeting, name;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dashboard_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");
        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        greeting = root.findViewById(R.id.greeting);
        name = root.findViewById(R.id.name);
        name.setText(mSharedPref.getString("registeredName","") + ".");

        if (currentTime < 12) {
            greeting.setText(R.string.greetings_morning);
        } else if (currentTime < 18) {
            greeting.setText(R.string.greetings_afternoon);
        } else {
            greeting.setText(R.string.greetings_evening);
        }

        return root;
    }
}