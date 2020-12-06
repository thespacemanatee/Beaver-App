package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;


public class AddUsersFragment extends Fragment {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private SharedPreferences mSharedPref;
    private String userID;
    private TextInputEditText addUsers;
    private CircularProgressButton addUsersBtn;
    private UsersAdapter adapter;
    private String groupName;
    private String groupID;
    private String groupImg;
    private boolean groupExists;
    private List<Map<String, Object>> users;
    private ArrayList<UserEntry> adaptedUsers = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private GroupEntry groupEntry;
    //Create ArrayList to store grpMemberIDs, HashMaps to store grpMemberNames and grpMemberImgs

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_users, container, false);
        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        Bundle bundle = this.getArguments();
        groupImg = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupId");
        groupExists = bundle.getBoolean("groupExists");

        addUsers = rootView.findViewById(R.id.addUsers);
        addUsersBtn = rootView.findViewById(R.id.addUsersBtn);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle(groupName);

        GroupEntry.GetGroupEntry getGroup = new GroupEntry.GetGroupEntry(groupID, 5000) {
            @Override
            public void onPostExecute() {
                groupEntry = getResult();
                mRecyclerView = rootView.findViewById(R.id.usersRecyclerView);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                mRecyclerView.setLayoutManager(layoutManager);
                populateRecyclerView();
                adapter = new UsersAdapter(adaptedUsers);
                mRecyclerView.setAdapter(adapter);
            }
        };
        getGroup.start();

        addUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUsersBtn.startAnimation();
                String user = addUsers.getText().toString();
                if (!user.isEmpty()) {
                    addUserToGroup(user);
                }
            }
        });

        return rootView;
    }

    private void addUserToGroup(String email) {

        CollectionReference collectionReferenceUsers = fStore.collection("users");
        Query queryEmail = collectionReferenceUsers.whereEqualTo("email", email);
        queryEmail.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
           @Override
           public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if (task.isSuccessful()) {
                   for (QueryDocumentSnapshot document : task.getResult()) {

                       userID = document.getId();

                       GroupEntry.UpdateGroupEntry addMember = new GroupEntry.UpdateGroupEntry(groupID,
                               GroupEntry.UpdateGroupEntry.FieldChange.MEMBER_LIST_ADD, userID, 5000) {
                           @Override
                           public void onPostExecute() {
                               UserEntry.GetUserEntry getUser = new UserEntry.GetUserEntry(userID, 5000) {
                                   @Override
                                   public void onPostExecute() {

                                       UserEntry userEntry = getResult();
                                       userEntry.addGroupId(groupID);

                                       UserEntry.UpdateUserEntry addUser = new UserEntry.UpdateUserEntry(userID,
                                               UserEntry.UpdateUserEntry.FieldChange.GROUPS_ADD, groupID, 5000) {
                                           @Override
                                           public void onPostExecute() {
                                               Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                                               addUsersBtn.revertAnimation();
                                               adaptedUsers.add(userEntry);
                                               adapter.notifyDataSetChanged();
                                           }
                                       };
                                       addUser.start();
                                   }
                               };
                               getUser.start();
                           }
                       };
                       addMember.start();

                   }
               }
           }
        });
        addUsersBtn.revertAnimation();
    }

    private void populateRecyclerView() {

        List<Object> members = groupEntry.getMember_list();
        for (Object member: members) {

            UserEntry.GetUserEntry getMemberInfo = new UserEntry.GetUserEntry((String) member, 5000) {
                @Override
                public void onPostExecute() {
                    adaptedUsers.add(getResult());
                    adapter.notifyDataSetChanged();
                }
            };
            getMemberInfo.start();
        }
    }
}