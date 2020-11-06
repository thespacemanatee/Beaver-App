package com.example.beever.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.beever.feature.CalendarFragment;
import com.example.beever.feature.DashboardFragment;
import com.example.beever.feature.GroupsFragment;
import com.example.beever.R;
import com.example.beever.feature.ToDoFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainDashboardFragment extends Fragment {

    ChipNavigationBar chipNavigationBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.main_dashboard_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");

        chipNavigationBar = root.findViewById(R.id.bottom_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_menu_dashboard,true);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();

        bottomMenu();

        return root;
    }

    private void bottomMenu() {

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;

                switch (i) {

                    case R.id.bottom_menu_dashboard:
                        fragment = new DashboardFragment();
                        break;

                    case R.id.bottom_menu_calendar:
                        fragment = new CalendarFragment();
                        break;

                    case R.id.bottom_menu_groups:
                        fragment = new GroupsFragment();
                        break;

                    case R.id.bottom_menu_todo:
                        fragment = new ToDoFragment();
                        break;
                }

                getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

            }
        });
    }
}
