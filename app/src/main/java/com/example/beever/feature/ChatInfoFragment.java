package com.example.beever.feature;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class ChatInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer) getActivity()).getSupportActionBar();
        View rootView = inflater.inflate(R.layout.fragment_chat_info, container, false);

        //Receive arguments from ChatFragment
        Bundle bundle = this.getArguments();
        int selectedGrpImg = bundle.getInt("selectedGrpImg");
        String selectedGrpId = bundle.getString("selectedGrpId");

        //Get chat_info_img in fragment_chat_info.xml and setImageResource
        ShapeableImageView chatImg = rootView.findViewById(R.id.chat_info_img);
        chatImg.setImageResource(selectedGrpImg);

        //Set the group members names
        ListView layout = rootView.findViewById(R.id.chat_info_group_members);
        layout.setScrollContainer(false);
        layout.setAdapter(new GroupMemberAdapter(getActivity()));

        return rootView;

    }

    ArrayList<String> grpMembers = new ArrayList<>();
    ArrayList<Integer> grpMemberImg = new ArrayList<>();
    {
        grpMembers.add("Claudia");
        grpMembers.add("Claudia");
        grpMembers.add("Claudia");
        grpMembers.add("Claudia");
        grpMembers.add("Claudia");

        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
        grpMemberImg.add(R.drawable.pink_circle);
    }

    class GroupMemberAdapter extends BaseAdapter {

        LayoutInflater inflater;
        GroupMemberAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() { return grpMembers.size(); }

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

            //ViewHolder for smoother scrolling
            GrpMemberViewHolder viewHolder;

            if (view == null) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new GrpMemberViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                view = inflater.inflate(R.layout.group_member_item, null);

                //Get ImageButton and TextView to populate
                viewHolder.memberImg = view.findViewById(R.id.grp_member_img);
                viewHolder.member = view.findViewById(R.id.grp_member);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (GrpMemberViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int selectedMemberImg = grpMemberImg.get(i);
            String selectedMember = grpMembers.get(i);

            //setImageResource for ImageButton and setText for TextView
            viewHolder.memberImg.setImageResource(selectedMemberImg);
            viewHolder.member.setText(selectedMember);

            return view;
        }

        //To reduce reloading of same layout
        class GrpMemberViewHolder {
            ShapeableImageView memberImg;
            TextView member;
        }

    }
}