
package com.example.beever.feature;

import android.content.Context;
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

import java.util.ArrayList;

public class GroupsFragment extends Fragment implements Populatable{

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID = fAuth.getUid();

    ArrayList<String> grpImages = new ArrayList<>();
    ArrayList<String> grpNames = new ArrayList<>();
    ArrayList<String> grpIds = new ArrayList<>();

    String addGrpBtnImg = Integer.toString(R.drawable.plus);
    String addGrpBtnText = "Add group...";
    GridAdapter adapter;
    View bottom_menu;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Groups");

        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        grpNames.clear();
        grpImages.clear();
        grpIds.clear();

        {
            //Append addGrpBtnImg and addGrpBtnText to beginning of each ArrayList
            grpNames.add(addGrpBtnText);
            grpImages.add(addGrpBtnImg);
            grpIds.add(null);
        }

        //Populate GridView in fragment_groups.xml with Groups
        GridView layout = rootView.findViewById(R.id.groupButtons);
        adapter = new GridAdapter(getActivity());
        layout.setAdapter(adapter);
        populateRecyclerView();

        return rootView;
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

    @Override
    public void populateRecyclerView() {
        UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry(userID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    Log.d("USER ENTRY", "success");
                    for (Object o: getResult().getGroups()) {
                        Log.d("GROUP", (String)o);
                        GroupEntry.GetGroupEntry groupGetter = new GroupEntry.GetGroupEntry((String)o, 5000) {
                            @Override
                            public void onPostExecute() {
                                if (isSuccessful()) {
                                    Log.d("GROUP ENTRY", getResult().getName());
                                    Log.d("GROUP RESULT", getResult().toString());
                                    Log.d("GROUP ID", getGroupId());
                                    grpIds.add(getGroupId());
                                    grpNames.add(getResult().getName());
                                    if (getResult().getDisplay_picture() == null) {
                                        grpImages.add("null");
                                    } else {
                                        grpImages.add(getResult().getDisplay_picture());
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        };
                        groupGetter.start();
                    }
                }
            }
        };
        userGetter.start();
    }

    //Class to populate GridView
    class GridAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;
        GridAdapter(Context c) {
            context = c;
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return grpNames.size();
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
            String selectedGrpImg = grpImages.get(i);
            String selectedGrpName = grpNames.get(i);
            String selectedGrpId = grpIds.get(i);

            //setText for TextView
            viewHolder.gridTxt.setText(selectedGrpName);
            Log.d("CURRENTLY ADAPTING", selectedGrpName + " to make it not fail");

            //Set onClick
            if (selectedGrpImg.equals(addGrpBtnImg) && selectedGrpName.equals(addGrpBtnText)) {
                //If gridImg is addGrpBtnImg and gridImgText is addGrpBtnText,

                //Set image for ShapeableImageView
                Glide.with(context).load(Integer.parseInt(selectedGrpImg)).into(viewHolder.gridImg);

                //Go to CreateGroupFragment
                viewHolder.gridImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Fade Out Nav Bar
                        Utils utils = new Utils(getContext());
                        utils.fadeOut();

                        //Go to CreateGroupFragment
                        CreateGroupFragment fragment = new CreateGroupFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    }
                });
            } else {
                //If gridImg is not addGrpBtnImg and gridImgText is not addGrpBtnText,

                //Set image for ShapeableImageView
                if (selectedGrpImg.equals("null")) {
                    Glide.with(context).load(R.drawable.pink_circle).centerCrop().into(viewHolder.gridImg);
                } else {
                    Glide.with(context).load(selectedGrpImg).centerCrop().into(viewHolder.gridImg);
                }

                //Go to IndivChatFragment
                viewHolder.gridImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Bundle arguments to send to ChatFragment
                        Bundle bundle = new Bundle();
                        //bundle.putParcelable("selectedGrpImg", selectedGrpImg);
                        bundle.putString("groupImage", selectedGrpImg);
                        bundle.putString("groupName", selectedGrpName);
                        bundle.putString("groupId", selectedGrpId);

                        //Fade Out Nav Bar
                        Utils utils = new Utils(getContext());
                        utils.fadeOut();

                        //Go to IndivChatFragment
                        IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                        indivGroupFragment.setArguments(bundle);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.fragment_container, indivGroupFragment, "openChat").addToBackStack(null).commit();
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