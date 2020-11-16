package com.example.beever.feature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {

    ArrayList<Integer> grpImages = new ArrayList<>();
    ArrayList<String> grpNames = new ArrayList<>();
    // get grpNames and grpImages from Firebase, and append it here iteratively
    {
        grpImages.add(R.drawable.pink_circle);
        grpImages.add(R.drawable.pink_circle);
        grpImages.add(R.drawable.pink_circle);
        grpImages.add(R.drawable.pink_circle);
        grpImages.add(R.drawable.pink_circle);

        grpNames.add("Test");
        grpNames.add("Test");
        grpNames.add("Test");
        grpNames.add("Test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Groups");

        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        GridView layout = rootView.findViewById(R.id.groupButtons);
        layout.setAdapter(new GridAdapter(getActivity()));

        return rootView;
    }

    class GridAdapter extends BaseAdapter {

        LayoutInflater gridInflater;
        GridAdapter(Context c) {
            gridInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return grpImages.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                //Inflate the layout
                view = gridInflater.inflate(R.layout.group_grid_item, null);

                // Add The Image
                ImageButton gridImg = (ImageButton) view.findViewById(R.id.grid_item_img);
                gridImg.setImageResource(grpImages.get(i));

                // Add The Text
                TextView gridTxt = (TextView) view.findViewById(R.id.grid_item_text);
                gridTxt.setText(grpNames.get(i));
            }

            return view;
        }
    }
}