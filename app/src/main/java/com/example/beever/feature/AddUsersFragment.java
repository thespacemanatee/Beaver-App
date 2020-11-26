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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private CircularProgressButton addUsersBtn;
    private UsersAdapter adapter;
    private String groupImage;
    private String groupName;
    private String groupID;
    private List<Map<String, Object>> users;
    private ArrayList<UserHelperClass> adaptedUsers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_add_users, container, false);
        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        userID = fAuth.getCurrentUser().getUid();

        Bundle bundle = this.getArguments();
        groupImage = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupID");

        imageUri = Uri.parse(groupImage);
        chatImg = rootView.findViewById(R.id.chat_img);
        addUsers = rootView.findViewById(R.id.addUsers);
        addUsersBtn = rootView.findViewById(R.id.addUsersBtn);

        populateRecyclerView();

        Glide.with(getActivity()).load(imageUri).into(chatImg);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle(groupName);

        addUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = addUsers.getText().toString();
                if (!user.isEmpty()) {
                    addUserToGroup(user);
                }
            }
        });

        return rootView;
    }

    private void addUserToGroup(String user) {
        DocumentReference documentReference0 = fStore.collection("groups").document(groupID);
        DocumentReference documentReference1 = fStore.collection("users").document(userID);
        Map<String, Object> memberMap = new HashMap<>();
        List<Map<String, Object>> members = new ArrayList<>();
        memberMap.put("name", mSharedPref.getString("registeredName", ""));
        memberMap.put("email", mSharedPref.getString("registeredEmail",""));
        memberMap.put("user_id", userID);
        documentReference0.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    // Update user group list with new group
                    if (document.exists()) {

                        Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();
                        documentReference0.update("member_list",  FieldValue.arrayUnion(memberMap)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "SAVED TO REFERENCE 1.5555", Toast.LENGTH_SHORT).show();
                            }
                        });
                        List<String> groups = new ArrayList<>();
                        groups.add(groupID);
                        documentReference1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    // Check if group list exists already, update if exist, if not create new group list
                                    if (document.exists()) {

                                        documentReference1.update("groups",  FieldValue.arrayUnion(groupID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getActivity(), "ADDED GROUP TO USER LIST", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        documentReference1.set(groups).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getActivity(), "CREATE NEW GROUP LIST FOR USER", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        });

                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void populateRecyclerView() {


        DocumentReference documentReference0 = fStore.collection("groups").document(groupID);
        documentReference0.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    // Check if group exists already, update if exist, if not create new group
                    if (document.exists()) {

                        Toast.makeText(getActivity(), "Group found", Toast.LENGTH_SHORT).show();
                        users = (List<Map<String, Object>>) document.get("member_list");

                    }
                }

                String name = null;
                String email = null;
                for (Map<String, Object> map: users) {
                    for (Map.Entry<String, Object> entry: map.entrySet()) {

                        if (entry.getKey().equals("name") && entry.getValue() != null) {
                            name = entry.getValue().toString();
                        }
                        if (entry.getKey().equals("email") && entry.getValue() != null) {
                            email = entry.getValue().toString();
                        }
                        UserHelperClass user = new UserHelperClass(name, email);

                        if (name != null && email != null) {
                            Toast.makeText(getActivity(), user.getName(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), user.getEmail(), Toast.LENGTH_SHORT).show();

                            adaptedUsers.add(user);
                        }
                    }
                }
                adapter = new UsersAdapter(adaptedUsers);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView list = view.findViewById(R.id.usersRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);
        adapter = new UsersAdapter(adaptedUsers);
        list.setAdapter(adapter);
    }
}