package com.example.beever.feature;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;

public class CreateGroupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Create a group");
        View rootView =  inflater.inflate(R.layout.fragment_create_group, container, false);

        //TODO: Let's get this

        return rootView;
    }
}