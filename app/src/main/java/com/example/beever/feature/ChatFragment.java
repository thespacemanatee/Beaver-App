package com.example.beever.feature;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.beever.R;
import com.google.android.material.imageview.ShapeableImageView;

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

        return rootView;
    }

    ArrayList<Integer> grpMemberImg = new ArrayList<>();
    ArrayList<String> texts = new ArrayList<>();
    {
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);

        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 1");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 2");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 3");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 4");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 5");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 6");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 7");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 8");
        texts.add("This is a message! I'm trying to make it really long. Hopefully it spans three lines. 9");
    }

    class BubblesAdapter extends BaseAdapter {

        LayoutInflater gridInflater;
        BubblesAdapter(Context c) {
            gridInflater = LayoutInflater.from(c);
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
                view = gridInflater.inflate(R.layout.chat_bubbles, null);

                //Get ImageButton and TextView to populate
                viewHolder.memberImg = view.findViewById(R.id.chat_member_img);
                viewHolder.text = view.findViewById(R.id.bubble);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (BubblesViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int img = grpMemberImg.get(i);
            String txt = texts.get(i);

            //setImageResource for ImageButton and setText for TextView
            viewHolder.memberImg.setImageResource(img);
            viewHolder.text.setText(txt);

            return view;
        }

        //To reduce reloading of same layout
        class BubblesViewHolder {
            ShapeableImageView memberImg;
            TextView text;
        }
    }
}