package com.example.beever.feature;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.database.ChatEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class ChatInfoFragment extends Fragment implements Populatable{

    private CircularProgressButton addUsersBtn;
    private ArrayList<String> grpMembers = new ArrayList<>();
    private ArrayList<String> grpMemberImg = new ArrayList<>();
    private GroupMemberAdapter adapter;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String groupId;
    private String groupName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        ((NavigationDrawer) getActivity()).getSupportActionBar();
        View rootView = inflater.inflate(R.layout.fragment_chat_info, container, false);

        //Receive arguments from ChatFragment
        Bundle bundle = this.getArguments();
        String selectedGrpImg = bundle.getString("groupImage");
        groupName = bundle.getString("groupName");
        groupId = bundle.getString("groupId");

        addUsersBtn = rootView.findViewById(R.id.addUsersBtn2);

        //Get chat_info_img in fragment_chat_info.xml and setImageResource
        ShapeableImageView chatImg = rootView.findViewById(R.id.chat_info_img);
        Glide.with(getContext()).load(selectedGrpImg).centerCrop().into(chatImg);

        //Set the group members names
        ListView layout = rootView.findViewById(R.id.chat_info_group_members);
        layout.setScrollContainer(false);
        adapter = new GroupMemberAdapter(getActivity());
        layout.setAdapter(adapter);
        populateRecyclerView();


        addUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("groupName", groupName);
                bundle.putString("groupId", groupId);
                bundle.putBoolean("groupExists", true);
                addUsersBtn.startAnimation();
                AddUsersFragment addUsersFragment = new AddUsersFragment();
                addUsersFragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null);
                transaction.replace(R.id.fragment_container, addUsersFragment).commit();
            }
        });

        return rootView;

    }

    @Override
    public void populateRecyclerView() {
        grpMembers.clear();
        grpMemberImg.clear();

        DocumentReference documentReference = fStore.collection("groups").document(groupId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> members = (List<String>) document.get("member_list");
                        Toast.makeText(getContext(), "Grabbed members", Toast.LENGTH_SHORT).show();
                        if (members != null) {
                            for (String member: members) {
                                UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry(member, 5000) {
                                    @Override
                                    public void onPostExecute() {
                                        grpMembers.add(getResult().getName());
                                        if (getResult().getDisplay_picture() == null) {
                                            grpMemberImg.add("null");
                                        } else {
                                            grpMemberImg.add(getResult().getDisplay_picture());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                };
                                userGetter.start();
                            }
                        }
                    }
                }
            }
        });
    }

    class GroupMemberAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;
        GroupMemberAdapter(Context c) {
            context = c;
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
            String selectedMemberImg = grpMemberImg.get(i);
            String selectedMember = grpMembers.get(i);

            //setImageResource for ImageButton and setText for TextView
            if (selectedMemberImg.equals("null")) {
                Glide.with(context).load(R.drawable.pink_circle).centerCrop().into(viewHolder.memberImg);
            } else {
                Glide.with(context).load(selectedMemberImg).centerCrop().into(viewHolder.memberImg);
            }
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