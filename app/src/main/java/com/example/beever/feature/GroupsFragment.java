
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

public class GroupsFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID = fAuth.getUid();

    ArrayList<String> grpImages = new ArrayList<>();
    ArrayList<String> grpIds = new ArrayList<>();

    String addGrpBtnImg = Integer.toString(R.drawable.plus);
    String addGrpBtnText = "Add group...";
    GridAdapter adapter;



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
        grpIds.clear();
        grpImages.clear();

        {
            //Append addGrpBtnImg and addGrpBtnText to beginning of each ArrayList
            grpIds.add( addGrpBtnText);
            grpImages.add( addGrpBtnImg);


            //Somehow only loads properly when this is here, check it out later
//            grpIds.add("Test");
//            grpImages.add("https://firebasestorage.googleapis.com/v0/b/beaver-app-7998c.appspot.com/o/groups%2FH8DKr5zp34Sf5xHVhwD6TJljIWh2TEST1%2Fgroup_image.jpg?alt=media&token=f44be457-b260-41d9-8429-96c040781257");

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
                                        Log.d("GROUP ENTRY", "success");
                                        Log.d("GROUP RESULT", getResult().toString());
                                        grpIds.add(getResult().getName());
                                        adapter.notifyDataSetChanged();
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

        //Populate GridView in fragment_groups.xml with Groups
        GridView layout = rootView.findViewById(R.id.groupButtons);
        adapter = new GridAdapter(getActivity());
        layout.setAdapter(adapter);

        return rootView;
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
            //Bitmap selectedGrpImg = grpImages.get(i);
            String selectedGrpImg = grpImages.get(i);
            String selectedGrpId = grpIds.get(i);

            //setText for TextView
            viewHolder.gridTxt.setText(selectedGrpId);
            Log.d("CURRENTLY ADAPTING", selectedGrpId+" to make it not fail");

            //Set onClick
            if (selectedGrpImg.equals(addGrpBtnImg) && selectedGrpId.equals(addGrpBtnText)) {
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
                        transaction.replace(R.id.fragment_container, fragment, "openChat").addToBackStack(null).commit();
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
                        bundle.putString("selectedGrpImg", selectedGrpImg);
                        bundle.putString("selectedGrpId", selectedGrpId);

                        //Fade Out Nav Bar
                        Utils utils = new Utils(getContext());
                        utils.fadeOut();

                        //Go to IndivChatFragment
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