package com.example.beever.feature;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IndivGroupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_indiv_group, container, false);

        //Receive arguments from GroupFragment
        Bundle bundle = this.getArguments();
        String groupName = bundle.getString("groupName");

        ((NavigationDrawer) getActivity()).getSupportActionBar().setTitle(groupName);

        //Set ViewPager
        ViewPager2 viewPager = rootView.findViewById(R.id.indiv_grp_swipe);
        viewPager.setAdapter(new StateAdapter(this, bundle));

        //Set Tabs
        ArrayList<String> tabTitles = new ArrayList<>();
        tabTitles.add("Chat");
        tabTitles.add("Gap Finder");
        tabTitles.add("Info");
        TabLayout tabLayout = rootView.findViewById(R.id.indiv_grp_tab);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

        //To ensure the Fragment Name is set correctly
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName().equals("dashboard")) {
                    ((NavigationDrawer) getActivity()).getSupportActionBar().setTitle("Dashboard");
                    getFragmentManager().popBackStack();
                } else if (getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName().equals("groups")) {
                    ((NavigationDrawer) getActivity()).getSupportActionBar().setTitle("Groups");
                    getFragmentManager().popBackStack();
                } else {
                    getFragmentManager().popBackStack();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return rootView;
    }

    //To create the tabs functionality
    class StateAdapter extends FragmentStateAdapter {
        Bundle bundle;

        public StateAdapter(@NonNull Fragment fragment, Bundle bundle) {
            super(fragment);
            this.bundle = bundle;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            //Choose between ChatFragment, GapFinderFragment, and ChatInfoFragment
            if (position == 0) {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(bundle);
                return chatFragment;
            } else if (position == 1) {
                GapFinderFragment gapFinderFragment = new GapFinderFragment();
                gapFinderFragment.setArguments(bundle);
                return gapFinderFragment;
            } else {
                ChatInfoFragment chatInfo = new ChatInfoFragment();
                chatInfo.setArguments(bundle);
                return chatInfo;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Utils utils = new Utils(getContext());
        utils.fadeIn();
    }


}