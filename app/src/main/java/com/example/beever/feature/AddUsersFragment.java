package com.example.beever.feature;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.admin.UserHelperClass;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AddUsersFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private ShapeableImageView chatImg;
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

        Bundle bundle = this.getArguments();
        groupImage = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupID");

        imageUri = Uri.parse(groupImage);
        chatImg = rootView.findViewById(R.id.chat_img);

        populateRecyclerView();



        Glide.with(getActivity()).load(imageUri).into(chatImg);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle(groupName);

        return rootView;
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
//                        Log.i("HELLLLO MY NAMES NINO", user.getName());
//                        Log.i("HELLLLO MY EMAILS NINO", user.getEmail());

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