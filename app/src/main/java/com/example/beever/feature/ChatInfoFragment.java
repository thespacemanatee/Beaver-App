package com.example.beever.feature;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.imageview.ShapeableImageView;

public class ChatInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer) getActivity()).getSupportActionBar().setTitle("Chat Information");
        View rootView = inflater.inflate(R.layout.fragment_chat_info, container, false);

        //Receive arguments from ChatFragment
        Bundle bundle = this.getArguments();
        int selectedGrpImg = bundle.getInt("selectedGrpImg");
        String selectedGrpId = bundle.getString("selectedGrpId");

        //Get chat_info_id in fragment_chat_info.xml and setText
        TextView chatId = rootView.findViewById(R.id.chat_info_id);
        chatId.setText(selectedGrpId);

        //Get chat_info_img in fragment_chat_info.xml and setImageResource
        ShapeableImageView chatImg = rootView.findViewById(R.id.chat_info_img);
        chatImg.setImageResource(selectedGrpImg);

        return rootView;

    }
}