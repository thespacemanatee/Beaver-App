
package com.example.beever.feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsFragment extends Fragment implements Populatable{

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID = fAuth.getUid();

    //Initialise global ArrayLists for storing information of Groups a User is in
    ArrayList<String> grpImages = new ArrayList<>();
    ArrayList<String> grpNames = new ArrayList<>();
    ArrayList<String> grpIds = new ArrayList<>();

    //For the Add Groups Button
    String addGrpBtnImg = Integer.toString(R.drawable.plus);
    String addGrpBtnText = "Create group";

    ImageView imageView;
    TextView textView;

    GridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Groups");

        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        imageView = rootView.findViewById(R.id.no_group_image);
        textView = rootView.findViewById(R.id.no_group_text);

        //Populate GridView in fragment_groups.xml with Groups
        GridView layout = rootView.findViewById(R.id.groupButtons);
        adapter = new GridAdapter(getActivity());
        layout.setAdapter(adapter);
        populateRecyclerView();

        return rootView;
    }

    @Override
    public void populateRecyclerView() {
        grpNames.clear();
        grpImages.clear();
        grpIds.clear();

        //Append addGrpBtnImg and addGrpBtnText to beginning of each ArrayList
        grpNames.add(addGrpBtnText);
        grpImages.add(addGrpBtnImg);
        grpIds.add(null);

        UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry(userID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    for (Object o: getResult().getGroups()) {
                        GroupEntry.GetGroupEntry groupGetter = new GroupEntry.GetGroupEntry((String)o, 5000) {
                            @Override
                            public void onPostExecute() {
                                if (isSuccessful()) {
                                    grpIds.add(getGroupId());
                                    grpNames.add(getResult().getName());
                                    grpImages.add(getResult().getDisplay_picture());
                                    if (grpIds.size() > 0) {
                                        imageView.setVisibility(View.GONE);
                                        textView.setVisibility(View.GONE);
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

    //Load Group Information from FireStore before going to next Fragment
    public void getGroupMemberInfo(String groupID, String groupImg, String groupName, Bundle bundle) {

        //Create ArrayList to store grpMemberIDs, HashMaps to store grpMemberNames and grpMemberImgs
        ArrayList<String> grpMemberIDs = new ArrayList<>();
        HashMap<String, String> grpMemberNames = new HashMap<>();
        HashMap<String, String> grpMemberImgs = new HashMap<>();

        //Get grpMemberIds, grpMemberNames, grpMemberImgs from FireStore
        GroupEntry.GetGroupEntry grpGetter = new GroupEntry.GetGroupEntry(groupID, 100000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    for (Object o: getResult().getMember_list()) {
                        Log.d("MEMBER ID", (String)o);
                        int full = getResult().getMember_list().size();
                        grpMemberIDs.add((String)o);
                        UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry((String)o, 100000) {
                            @Override
                            public void onPostExecute() {
                                if (isSuccessful()) {
                                    grpMemberImgs.put((String)o, getResult().getDisplay_picture());
                                    grpMemberNames.put((String)o, getResult().getName());

                                    if (grpMemberNames.size() == full) {
                                        //Add everything to bundle
                                        bundle.putStringArrayList("grpMemberIDs", grpMemberIDs);
                                        bundle.putSerializable("grpMemberImgs", grpMemberImgs);
                                        bundle.putSerializable("grpMemberNames", grpMemberNames);
                                        bundle.putString("groupImage", groupImg);
                                        bundle.putString("groupName", groupName);
                                        bundle.putString("groupId", groupID);

                                        //Fade Out Nav Bar
                                        Utils utils = new Utils(getContext());
                                        utils.fadeOut();

                                        //Go to IndivChatFragment
                                        IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                                        indivGroupFragment.setArguments(bundle);
                                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                        transaction.add(R.id.fragment_container, indivGroupFragment, "openChat").addToBackStack("groups").commit();
                                    }
                                }
                            }
                        };
                        userGetter.start();
                    }
                }
            }
        };
        grpGetter.start();

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

            //Set onClick
            if (selectedGrpImg.equals(addGrpBtnImg) && selectedGrpName.equals(addGrpBtnText)) {
                //If gridImg is addGrpBtnImg and gridImgText is addGrpBtnText, the Add Group Button is created.

                //Set Add Group image for ShapeableImageView
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

                viewHolder.gridImg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AddToDashboardDialogFragment dialog = new AddToDashboardDialogFragment(selectedGrpId, userID);
                        assert getFragmentManager() != null;
                        dialog.show(getFragmentManager(), "");
                        return true;
                    }
                });

                //Set image for ShapeableImageView
                if (selectedGrpImg ==  null) {
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
                        getGroupMemberInfo(selectedGrpId, selectedGrpImg, selectedGrpName, bundle);
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