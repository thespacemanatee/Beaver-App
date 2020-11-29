package com.example.beever.feature;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.TextView;

import com.example.beever.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        //Show Chat Bubbles
        ListView layout = rootView.findViewById(R.id.bubbles_area);
        layout.setAdapter(new BubblesAdapter(getContext()));

        //Create Send Button
        ImageButton sendButton = rootView.findViewById(R.id.send_button);
        EditText editText = rootView.findViewById(R.id.send_message);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = editText.getText().toString();
                if (!newText.equals("")) {
                    grpMemberImg.add(R.drawable.beever_logo);
                    texts.add(newText);
                    Log.d("CHECK NEW TEXT", newText);
                    times.add(new Timestamp(System.currentTimeMillis()));
                    editText.setText("");
                    layout.setAdapter(new BubblesAdapter(getContext()));
                }
            }
        });

        return rootView;
    }

    ArrayList<Integer> grpMemberImg = new ArrayList<>();
    ArrayList<String> texts = new ArrayList<>();
    ArrayList<Timestamp> times = new ArrayList<>();
    {
        for (int i=0; i<9; i++) {
            grpMemberImg.add(R.drawable.pink_circle);
            texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. "+i);
            times.add(new Timestamp(System.currentTimeMillis()));
        }
    }

    class BubblesAdapter extends BaseAdapter {

        LayoutInflater inflater;
        BubblesAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() { return texts.size(); }

        @Override
        public Object getItem(int i) { return i; }

        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            //ViewHolder for smoother scrolling
           BubblesViewHolder viewHolder;

            if (view == null) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new BubblesViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                view = inflater.inflate(R.layout.chat_bubbles, null);

                //Get ImageButton and TextView to populate
                viewHolder.memberImg = view.findViewById(R.id.chat_member_img);
                viewHolder.text = view.findViewById(R.id.bubble);
                viewHolder.time = view.findViewById(R.id.bubble_time);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (BubblesViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int img = grpMemberImg.get(i);
            String txt = texts.get(i);
            String timestamp = times.get(i).toString().substring(11, 16);

            //setImageResource for ImageButton and setText for TextView
            viewHolder.memberImg.setImageResource(img);
            viewHolder.text.setText(txt);
            viewHolder.time.setText(timestamp);

            return view;
        }

        //To reduce reloading of same layout
        class BubblesViewHolder {
            ShapeableImageView memberImg;
            TextView text;
            TextView time;
        }
    }


}