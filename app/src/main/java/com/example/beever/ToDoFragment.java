package com.example.beever;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.beever.navigation.NavigationDrawer;

public class ToDoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("To-Do");

        return inflater.inflate(R.layout.fragment_to_do, container, false);
    }
}