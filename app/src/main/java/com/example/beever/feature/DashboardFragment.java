package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Calendar;

public class DashboardFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID = fAuth.getUid();
    int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private SharedPreferences mSharedPref;
    TextView greeting, name;
    DashBoardGroupsAdapter adapter;
    View bottom_menu;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dashboard_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Dashboard");
        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        greeting = root.findViewById(R.id.greeting);
        name = root.findViewById(R.id.name);

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
        adapter = new DashBoardGroupsAdapter(getActivity());
        layout.setAdapter(adapter);
        populateRecyclerView();

        return root;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        //Fade in Nav Bar
        bottom_menu = getActivity().findViewById(R.id.bottom_menu);
        if (bottom_menu.getVisibility() == View.GONE) {
            Utils utils = new Utils(getContext());
            utils.fadeIn();
        }
    }

    ArrayList<String> dbGrpImgs = new ArrayList<>();
    ArrayList<String> dbGrpNames = new ArrayList<>();
    ArrayList<String> dbGrpIds = new ArrayList<>();
    public void populateRecyclerView() {
        dbGrpIds.clear();
        dbGrpImgs.clear();
        dbGrpNames.clear();
        UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry(userID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    Log.d("USER ENTRY", "success");
                    for (Object o: getResult().getDashboard_grps()) {
                        if (o != null) {
                            Log.d("GROUP", (String) o);
                            GroupEntry.GetGroupEntry groupGetter = new GroupEntry.GetGroupEntry((String) o, 5000) {
                                @Override
                                public void onPostExecute() {
                                    if (isSuccessful()) {
                                        Log.d("GROUP ENTRY", "success");
                                        Log.d("GROUP RESULT", getResult().toString());
                                        dbGrpIds.add(getGroupId());
                                        dbGrpNames.add(getResult().getName());
                                        adapter.notifyDataSetChanged();
                                        if (getResult().getDisplay_picture() == null) {
                                            dbGrpImgs.add("null");
                                        } else {
                                            dbGrpImgs.add(getResult().getDisplay_picture());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            };
                            groupGetter.start();
                        }
                    }
                }
            }
        };
        userGetter.start();
    }

    class DashBoardGroupsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;
        DashBoardGroupsAdapter(Context c) {
            context = c;
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
            String selectedGrpImg = dbGrpImgs.get(i);
            String selectedGrpName = dbGrpNames.get(i);
            String selectedGrpId = dbGrpIds.get(i);

            //setImageResource for ImageButton and setText for TextView
            if (selectedGrpImg.equals("null")) {
                Glide.with(context).load(R.drawable.pink_circle).centerCrop().into(viewHolder.gridImg);
            } else {
                Glide.with(context).load(selectedGrpImg).centerCrop().into(viewHolder.gridImg);
            }
            viewHolder.gridTxt.setText(selectedGrpName);

            //Set onClick
            viewHolder.gridImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Bundle arguments to send to ChatFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("groupImage", selectedGrpImg);
                    bundle.putString("groupName", selectedGrpName);
                    bundle.putString("groupId", selectedGrpId);

                    //Fade Out Nav Bar
                    Utils utils = new Utils(getContext());
                    utils.fadeOut();

                    //Go to ChatFragment
                    IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                    indivGroupFragment.setArguments(bundle);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.fragment_container, indivGroupFragment, "openChat").addToBackStack(null).commit();
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