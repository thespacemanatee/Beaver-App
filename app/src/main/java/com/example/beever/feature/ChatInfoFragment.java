package com.example.beever.feature;

import android.app.ActionBar;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
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

        //Get chat_info_id in fragment_chat_info.xml and setText
        TextView chatId = rootView.findViewById(R.id.chat_info_id);
        chatId.setText(selectedGrpId);

        //Get chat_info_img in fragment_chat_info.xml and setImageResource
        ShapeableImageView chatImg = rootView.findViewById(R.id.chat_info_img);
        chatImg.setImageResource(selectedGrpImg);

        //Set ViewPager
        ViewPager2 viewPager = rootView.findViewById(R.id.chat_info_swipe);
        viewPager.setAdapter(new ChatInfoStateAdapter(this));

        //Set Tabs
        ArrayList<Integer> tabTitles = new ArrayList<>();
        tabTitles.add(R.string.chat_info_media);
        tabTitles.add(R.string.chat_info_files);
        tabTitles.add(R.string.chat_info_links);
        TabLayout tabLayout = rootView.findViewById(R.id.chat_info_tabs);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

        return rootView;

    }

    class ChatInfoStateAdapter extends FragmentStateAdapter {

        public ChatInfoStateAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                ChatInfoMediaFragment media = new ChatInfoMediaFragment();
                return media;
            } else if (position == 1) {
                ChatInfoFilesFragment files = new ChatInfoFilesFragment();
                return files;
            } else {
                ChatInfoLinksFragment links = new ChatInfoLinksFragment();
                return links;
            }

        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}