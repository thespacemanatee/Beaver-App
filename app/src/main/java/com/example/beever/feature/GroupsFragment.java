
package com.example.beever.feature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.admin.MainActivity;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.example.beever.navigation.SpaceItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userID = fAuth.getUid();

    ArrayList<Integer> grpImages = new ArrayList<>();
    ArrayList<String> grpIds = new ArrayList<>();
    {
        for (int i=0; i<5; i++) {
            grpIds.add("Test "+ i);
        }

        for (int i=0; i<5; i++) {
            grpImages.add(R.drawable.beever_logo);
        }
    }
    int addGrpBtnImg = R.drawable.ic_baseline_add_40;
    String addGrpBtnText = "Add group...";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Groups");

        String userId = fAuth.getCurrentUser().getUid();

        //Fade in Nav Bar
        View bottom_menu = getActivity().findViewById(R.id.bottom_menu);
        if (bottom_menu.getVisibility() == View.GONE) {
            Utils utils = new Utils(getContext());
            utils.fadeIn();
        }

        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        for (int i=0; i<grpIds.size(); i++) {
            Log.d("GROUPS", grpIds.get(i));
        }

        //Populate GridView in fragment_groups.xml with Groups
        GridView layout = rootView.findViewById(R.id.groupButtons);
        layout.setAdapter(new GridAdapter(getActivity()));

        return rootView;
    }

    //Append addGrpBtnImg and addGrpBtnText to beginning of each ArrayList
    {
        grpIds.add(0, addGrpBtnText);
        grpImages.add(0, addGrpBtnImg);
    }

    //Class to populate GridView
    class GridAdapter extends BaseAdapter {

        LayoutInflater inflater;
        GridAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return grpIds.size();
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
            ViewHolder viewHolder;

            if (view == null) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new ViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                view = inflater.inflate(R.layout.group_grid_item, null);

                //Get ImageButton and TextView to populate
                viewHolder.gridImg = view.findViewById(R.id.grid_item_img);
                viewHolder.gridTxt = view.findViewById(R.id.grid_item_text);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (ViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int selectedGrpImg = grpImages.get(i);
            String selectedGrpId = grpIds.get(i);

            //setImageResource for ImageButton and setText for TextView
            viewHolder.gridImg.setImageResource(selectedGrpImg);
            viewHolder.gridTxt.setText(selectedGrpId);

            //Set onClick
            if (selectedGrpImg == addGrpBtnImg && selectedGrpId == addGrpBtnText) {
                //If gridImg is addGrpBtnImg and gridImgText is addGrpBtnText,
                //Fix colours
                viewHolder.gridImg.setBackgroundColor(getResources().getColor(R.color.grey));
                viewHolder.gridImg.setColorFilter(getResources().getColor(R.color.beever_blue));
                //Go to AddGroupFragment
                viewHolder.gridImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Fade Out Nav Bar
                        Utils utils = new Utils(getContext());
                        utils.fadeOut();

                        //Go to ChatFragment
                        CreateGroupFragment fragment = new CreateGroupFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment, "openChat").addToBackStack(null).commit();
                    }
                });
            } else {
                //If gridImg is not addGrpBtnImg and gridImgText is not addGrpBtnText,
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
            }

            return view;
        }

        //To reduce reloading of same layout
        class ViewHolder {
            ShapeableImageView gridImg;
            TextView gridTxt;
        }
    }
}