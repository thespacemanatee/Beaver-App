package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.admin.UserHelperClass;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;


public class AddUsersFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private SharedPreferences mSharedPref;
    private String userID;
    private Uri imageUri;
    private TextInputEditText addUsers;
    private ShapeableImageView chatImg;
    private CircularProgressButton addUsersBtn, confirmUsersBtn;
    private UsersAdapter adapter;
//    private String groupImage;
    private String groupName;
    private String groupID;
    private List<Map<String, Object>> users;
    private ArrayList<UserHelperClass> adaptedUsers = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_add_users, container, false);
        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        Bundle bundle = this.getArguments();
//        groupImage = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupId");

//        imageUri = Uri.parse(groupImage);
//        chatImg = rootView.findViewById(R.id.chat_img);
        addUsers = rootView.findViewById(R.id.addUsers);
        addUsersBtn = rootView.findViewById(R.id.addUsersBtn);
        confirmUsersBtn = rootView.findViewById(R.id.confirm_users);

//        Glide.with(getActivity()).load(imageUri).into(chatImg);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle(groupName);

//        UserHelperClass user = new UserHelperClass(mSharedPref.getString("registeredName", ""),
//                mSharedPref.getString("registeredEmail", ""),
//                fAuth.getCurrentUser().getUid());
//        adaptedUsers.add(user);

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

        confirmUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmUsersBtn.startAnimation();
                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupID);
                bundle.putString("groupName", groupName);
                //Go to Individual Groups Fragment
                IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                indivGroupFragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, indivGroupFragment, "openChat").commit();
            }
        });

        return rootView;
    }

    private void addUserToGroup(String email) {
        CollectionReference collectionReferenceUsers = fStore.collection("users");
        CollectionReference collectionReferenceGroups = fStore.collection("groups");
        Query queryEmail = collectionReferenceUsers.whereEqualTo("email", email);
//        List<String> members = new ArrayList<>();
//        Map<String, Object> memberMap = new HashMap<>();
        queryEmail.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        String name = document.getString("name");
                        userID = document.getId();
                        collectionReferenceUsers.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    ArrayList<String> groups = (ArrayList<String>) document.get("groups");
                                    if (groups.contains(groupID)) {
                                        Toast.makeText(getActivity(), "User already added", Toast.LENGTH_SHORT).show();
                                    } else {

//                                        memberMap.put("name", name);
//                                        memberMap.put("email", email);
//                                        members.add(userID);
                                        UserHelperClass user = new UserHelperClass(name, email, userID);
                                        adaptedUsers.add(user);
                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(getActivity(), "USER FOUND: " + document.getString("name"), Toast.LENGTH_SHORT).show();

                                        DocumentReference documentReferenceGroups = collectionReferenceGroups.document(groupID);
                                        DocumentReference documentReferenceUsers = collectionReferenceUsers.document(userID);

                                        documentReferenceGroups.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    // Update user group list with new group
                                                    if (document.exists()) {

                                                        Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();
                                                        documentReferenceGroups.update("member_list",  FieldValue.arrayUnion(userID));
                                                        List<String> groups = new ArrayList<>();
                                                        groups.add(groupID);
                                                        documentReferenceUsers.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();

                                                                    // Check if group list exists already, update if exist, if not create new group list
                                                                    if (document.exists()) {

                                                                        documentReferenceUsers.update("groups",  FieldValue.arrayUnion(groupID));

                                                                    } else {
                                                                        Map<String, Object> groupMap = new HashMap<>();
                                                                        groupMap.put("groups", groups);
                                                                        documentReferenceUsers.set(groupMap);
                                                                    }
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            }
                                        });

                                    }
                                }

                            }
                        });
                    }
                }
            }
        });
        addUsersBtn.revertAnimation();


    }

    private void populateRecyclerView() {

        DocumentReference documentReferenceGroups = fStore.collection("groups").document(groupID);
        documentReferenceGroups.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        List<String> members = (List<String>) document.get("member_list");
                        for (String member: members) {
                            DocumentReference documentReferenceUsers = fStore.collection("users").document(member);
                            documentReferenceUsers.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            String name = document.getString("name");
                                            String email = document.getString("email");
                                            String userID = document.getId();
                                            UserHelperClass user = new UserHelperClass(name, email, userID);
                                            adaptedUsers.add(user);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.usersRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        populateRecyclerView();
        adapter = new UsersAdapter(adaptedUsers);
        mRecyclerView.setAdapter(adapter);
        setUpItemTouchHelper();
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                adapter.remove(swipedPosition);
                DocumentReference documentReferenceGroups = fStore.collection("groups").document(groupID);
                DocumentReference documentReferenceUser = fStore.collection("users")
                        .document(adaptedUsers.get(swipedPosition).getUserID());
                documentReferenceUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
//                                Map<String, Object> memberMap = new HashMap<>();
//                                String name = document.getString("name");
//                                String email = document.getString("email");
                                userID = document.getId();
//                                memberMap.put("name", name);
//                                memberMap.put("email", email);
//                                memberMap.put("user_id", userID);
//                                List<String> members = new ArrayList<>();
//                                members.add(userID);
                                documentReferenceUser.update("groups", FieldValue.arrayRemove(groupID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Removed from group in users ref", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                documentReferenceGroups.update("member_list", FieldValue.arrayRemove(userID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Removed from group in groups ref", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }
                });
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }
}