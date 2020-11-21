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

public class ChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        //Receive arguments from GroupFragment
        Bundle bundle = this.getArguments();
        int selectedGrpImg = bundle.getInt("selectedGrpImg");
        String selectedGrpId = bundle.getString("selectedGrpId");

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle(selectedGrpId);

        //Get chat_id in fragment_chat.xml and setText
        TextView chatId = rootView.findViewById(R.id.chat_id);
        chatId.setText("Chat");

        //Get chat_img in fragment_chat.xml and setImageResource
        ShapeableImageView chatImg = rootView.findViewById(R.id.chat_img);
        chatImg.setImageResource(selectedGrpImg);

        //Open ChatInformationFragment
        chatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "works", Toast.LENGTH_SHORT).show();
                ChatInfoFragment chatInfoFragment = new ChatInfoFragment();
                chatInfoFragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, chatInfoFragment, "openChatInfo").addToBackStack(null).commit();
            }
        });

        //Populate ListView with Chat Bubbles

        return rootView;
    }
}