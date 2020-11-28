package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Calendar;

public class DashboardFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private SharedPreferences mSharedPref;
    TextView greeting, name;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dashboard_fragment, container, false);

        //Fade in Nav Bar
        View bottom_menu = getActivity().findViewById(R.id.bottom_menu);
        if (bottom_menu.getVisibility() == View.GONE) {
            Utils utils = new Utils(getContext());
            utils.fadeIn();
        }

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");
        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        greeting = root.findViewById(R.id.greeting);
        name = root.findViewById(R.id.name);

        FirebaseUser fUser = fAuth.getCurrentUser();
        name.setText(mSharedPref.getString("registeredName", "Beever") + ".");

        if (currentTime < 12) {
            greeting.setText(R.string.greetings_morning);
        } else if (currentTime < 18) {
            greeting.setText(R.string.greetings_afternoon);
        } else {
            greeting.setText(R.string.greetings_evening);
        }

        //Populate GridView in dashboard_fragment.xml with Groups
        GridView layout = root.findViewById(R.id.dashboard_groups);
        layout.setAdapter(new DashBoardGroupsAdapter(getActivity()));

        return root;
    }

    ArrayList<Integer> dbGrpImgs = new ArrayList<>();
    ArrayList<String> dbGrpIds = new ArrayList<>();
    {
        dbGrpImgs.add(R.drawable.profile);
        dbGrpImgs.add(R.drawable.beever_logo);
        dbGrpImgs.add(R.drawable.beever_logo_blue);

        dbGrpIds.add("Test 1");
        dbGrpIds.add("Test 2");
        dbGrpIds.add("Test 3");
    }

    class DashBoardGroupsAdapter extends BaseAdapter {

        LayoutInflater inflater;
        DashBoardGroupsAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return dbGrpImgs.size();
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
            //ViewHolder for smoother scrolling
            DashBoardViewHolder viewHolder;

            if (view == null) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new DashBoardViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                view = inflater.inflate(R.layout.group_grid_item, null);

                //Get ImageButton and TextView to populate
                viewHolder.gridImg = view.findViewById(R.id.grid_item_img);
                viewHolder.gridTxt = view.findViewById(R.id.grid_item_text);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (DashBoardViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int selectedGrpImg = dbGrpImgs.get(i);
            String selectedGrpId = dbGrpIds.get(i);

            //setImageResource for ImageButton and setText for TextView
            viewHolder.gridImg.setImageResource(selectedGrpImg);
            viewHolder.gridTxt.setText(selectedGrpId);

            //Set onClick
            viewHolder.gridImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Bundle arguments to send to ChatFragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("selectedGrpImg", selectedGrpImg);
                    bundle.putString("selectedGrpId", selectedGrpId);

                    //Fade Out Nav Bar
                    Utils utils = new Utils(getContext());
                    utils.fadeOut();

                    //Go to ChatFragment
                    IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                    indivGroupFragment.setArguments(bundle);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, indivGroupFragment, "openChat").addToBackStack(null).commit();
                }
            });
            return view;
        }

        //To reduce reloading of same layout
        class DashBoardViewHolder {
            ShapeableImageView gridImg;
            TextView gridTxt;
        }
    }
}