package com.example.beever.feature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupFragment extends Fragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private SharedPreferences mSharedPref;
    private String userID;
    private String groupName;
    private TextInputEditText groupNameText;
    private CircleImageView groupImageView;
    private CircularProgressButton createBtn;
    private Uri imageUri;
    private Uri newImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Create a group");
        View rootView =  inflater.inflate(R.layout.fragment_create_group, container, false);

        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        groupNameText = rootView.findViewById(R.id.group_name_selection);
        groupImageView = rootView.findViewById(R.id.group_picture_selection);
        userID = fAuth.getCurrentUser().getUid();

        groupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2000);
            }
        });

        createBtn = rootView.findViewById(R.id.create_group);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBtn.startAnimation();
                groupName = groupNameText.getText().toString();
                if (imageUri != null && groupName != null) {
                    uploadImage(imageUri);
                } else {
                    Toast.makeText(getActivity(),"Missing info!", Toast.LENGTH_SHORT).show();
                    createBtn.revertAnimation();
                }
            }
        });


        return rootView;
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileReference = storageReference.child("groups/" + generateGroupID() + "/group_image.jpg");
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newImageUri = uri;
                        uploadToFirebase(newImageUri.toString(),groupName,generateGroupID());
                    }
                });
            }
        });
    }

    private String generateGroupID() {
        return userID + groupName.replaceAll("\\s+","");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2000) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri = data.getData();
                Glide.with(getActivity()).load(imageUri).into(groupImageView);
            }
        }
    }

    private void uploadToFirebase(String imageUri, String groupName, String groupID) {
        DocumentReference documentReference0 = fStore.collection("groups").document(groupID);
        DocumentReference documentReference1 = fStore.collection("users").document(userID);
        Map<String, Object> map0 = new HashMap<>();
//        Map<String, Object> memberMap = new HashMap<>();
        List<String> members = new ArrayList<>();
//        List<Map<String, Object>> members = new ArrayList<>();
//        memberMap.put("name", mSharedPref.getString("registeredName", ""));
//        memberMap.put("email", mSharedPref.getString("registeredEmail",""));
//        memberMap.put("user_id", userID);
        members.add(userID);
        map0.put("group_name", groupName);
        map0.put("display_picture", imageUri);
        map0.put("member_list", members);

        documentReference0.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    // Check if group exists already, update if exist, if not create new group
                    if (document.exists()) {

                        Toast.makeText(getActivity(), "Group name unavailable", Toast.LENGTH_SHORT).show();

                    } else {
                        documentReference0.set(map0).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Group creation successful", Toast.LENGTH_SHORT).show();
                                List<String> groups = new ArrayList<>();
                                groups.add(groupID);
                                documentReference1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();

                                            // Check if group exists already, update if exist, if not create new group
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

                                Bundle bundle = new Bundle();
                                bundle.putString("imageUri", imageUri);
                                bundle.putString("groupName", groupName);
                                bundle.putString("groupID", groupID);

                                AddUsersFragment addUsersFragment = new AddUsersFragment();
                                addUsersFragment.setArguments(bundle);
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, addUsersFragment).commit();
                            }
                        });
                    }
                }
            }
        });

        createBtn.revertAnimation();
    }
}