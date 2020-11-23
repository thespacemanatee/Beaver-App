package com.example.beever.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.beever.feature.CalendarFragment;
import com.example.beever.feature.DashboardFragment;
import com.example.beever.feature.GroupsFragment;
import com.example.beever.R;
import com.example.beever.feature.ToDoFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.jetbrains.annotations.NotNull;

public class MainDashboardFragment extends Fragment {

    ChipNavigationBar chipNavigationBar;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStateAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.main_dashboard_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = root.findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        chipNavigationBar = root.findViewById(R.id.bottom_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_menu_dashboard,true);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (positionOffset < 0.01) {
                    if (position == 0) {
                        chipNavigationBar.setItemSelected(R.id.bottom_menu_dashboard,true);
                    } else if (position == 1) {
                        chipNavigationBar.setItemSelected(R.id.bottom_menu_calendar,true);
                    } else if (position == 2) {
                        chipNavigationBar.setItemSelected(R.id.bottom_menu_groups,true);
                    } else if (position == 3) {
                        chipNavigationBar.setItemSelected(R.id.bottom_menu_todo,true);
                    }
                }

            }
        });

        bottomMenu();

        return root;
    }

    /**
     * A simple pager adapter that represents the 4 menus, in
     * sequence.
     */
    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(Fragment fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case 0:
                    return new DashboardFragment();

                case 1:
                    return new CalendarFragment();

                case 2:
                    return new GroupsFragment();

                case 3:
                    return new ToDoFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    private void bottomMenu() {

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
//                Fragment fragment = null;

                if (i == R.id.bottom_menu_dashboard) {
                    viewPager.setCurrentItem(0, true);
                } else if (i == R.id.bottom_menu_calendar) {
                    viewPager.setCurrentItem(1, true);
                } else if (i == R.id.bottom_menu_groups) {
                    viewPager.setCurrentItem(2, true);
                } else if (i == R.id.bottom_menu_todo) {
                    viewPager.setCurrentItem(3, true);
                }


            }
        });
    }
}
