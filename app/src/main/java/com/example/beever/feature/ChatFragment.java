package com.example.beever.feature;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.TextView;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;

public class ChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Chat");
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        return rootView;
    }
}