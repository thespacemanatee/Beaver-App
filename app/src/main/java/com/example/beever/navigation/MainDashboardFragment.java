package com.example.beever.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.feature.CalendarFragment;
import com.example.beever.feature.DashboardFragment;
import com.example.beever.feature.GroupsFragment;
import com.example.beever.R;
import com.example.beever.feature.ToDoFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class MainDashboardFragment extends Fragment {

    private static final String GROUP_ENTRIES = "groupEntries";
    private static final String GROUP_IDS = "groupIds";
    private UserEntry userEntry;
    private ArrayList<GroupEntry> groupEntries = new ArrayList<>();
    private ArrayList<String> groupIds = new ArrayList<>();
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userId;
    private final String USER_ENTRY = "userEntry";
    ChipNavigationBar chipNavigationBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.main_dashboard_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");

        chipNavigationBar = root.findViewById(R.id.bottom_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_menu_dashboard,true);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();

        userId = fAuth.getCurrentUser().getUid();

        UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(userId, 5000) {
            @Override
            public void onPostExecute() {
                userEntry = getResult();
                getGroupEntries(userEntry);
            }
        };
        getUserEntry.start();

        UserEntry.UserEntryListener userEntryListener = new UserEntry.UserEntryListener(userId, 5000) {
            @Override
            public void onPreListening() {

            }

            @Override
            public void onListenerUpdate() {
                if (getStateChange()== StateChange.DASHBOARD_GRPS || getStateChange()== StateChange.DISPLAY_PICTURE
                        || getStateChange()== StateChange.EMAIL || getStateChange()== StateChange.GROUPS
                        || getStateChange()== StateChange.NAME || getStateChange()== StateChange.TODO_LIST
                        || getStateChange()== StateChange.USER_EVENTS || getStateChange()== StateChange.USERNAME) {
                    userEntry = getResult();
                    getGroupEntries(userEntry);
                }
            }

            @Override
            public void onSetupFailure() {

            }
        };
        userEntryListener.start();

        bottomMenu();

        return root;
    }

    private void getGroupEntries(UserEntry userEntry) {
        groupEntries.clear();
        groupIds.clear();
        for (Object o: userEntry.getGroups()) {

            GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry((String) o, 5000) {
                @Override
                public void onPostExecute() {
                    groupEntries.add(getResult());
                    groupIds.add(getGroupId());
                }
            };
            getGroupEntry.start();

        }
    }

    private void bottomMenu() {

        //TODO: Try to bundle data in when switching fragments to prefetch data
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                Bundle bundle = new Bundle();
                bundle.putParcelable(USER_ENTRY, userEntry);
                bundle.putParcelableArrayList(GROUP_ENTRIES, groupEntries);
                bundle.putStringArrayList(GROUP_IDS, groupIds);

                switch (i) {

                    case R.id.bottom_menu_dashboard:
                        fragment = new DashboardFragment();
                        fragment.setArguments(bundle);
                        break;

                    case R.id.bottom_menu_calendar:
                        fragment = new CalendarFragment();
                        fragment.setArguments(bundle);
                        break;

                    case R.id.bottom_menu_groups:
                        fragment = new GroupsFragment();
                        fragment.setArguments(bundle);
                        break;

                    case R.id.bottom_menu_todo:
                        fragment = new ToDoFragment();
                        fragment.setArguments(bundle);
                        break;
                }

                getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

            }
        });
    }
}
