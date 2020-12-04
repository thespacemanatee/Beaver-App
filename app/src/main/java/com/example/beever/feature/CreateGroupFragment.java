package com.example.beever.feature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
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
import com.google.firebase.firestore.auth.User;
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

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((NavigationDrawer) getActivity()).getSupportActionBar().setTitle("Groups");
                getFragmentManager().popBackStack();
                //Fade in Nav Bar
                Utils utils = new Utils(getContext());
                utils.fadeIn();

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);


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

        DocumentReference documentReference = fStore.collection("groups").document(groupID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    // Check if group exists already, update if exist, if not create new group
                    if (document.exists()) {

                      Toast.makeText(getActivity(), "Group name unavailable", Toast.LENGTH_SHORT).show();
                      createBtn.revertAnimation();

                    } else {

                        List<Object> members = new ArrayList<>();
                        members.add(userID);
                        GroupEntry groupEntry = new GroupEntry();
                        groupEntry.setName(groupName);
                        groupEntry.setDisplay_picture(imageUri);
                        groupEntry.setMember_list(members);

                        GroupEntry.SetGroupEntry createGroup = new GroupEntry.SetGroupEntry(groupEntry, groupID, 5000) {
                            @Override
                            public void onPostExecute() {

                                Toast.makeText(getContext(), "Group created successfully", Toast.LENGTH_SHORT).show();

                                UserEntry.GetUserEntry getUser = new UserEntry.GetUserEntry(userID, 5000) {
                                    @Override
                                    public void onPostExecute() {

                                        UserEntry userEntry = getResult();
                                        userEntry.addGroupId(groupID);

                                        UserEntry.SetUserEntry addUser = new UserEntry.SetUserEntry(userEntry, userID, 5000) {
                                            @Override
                                            public void onPostExecute() {
                                                Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                                                getGroupMemberInfo(groupID);
//                                                Bundle bundle = new Bundle();
//                                                bundle.putString("imageUri", imageUri);
//                                                bundle.putString("groupName", groupName);
//                                                bundle.putString("groupId", groupID);
//                                                bundle.putBoolean("groupExists", false);
//
//                                                AddUsersFragment addUsersFragment = new AddUsersFragment();
//                                                addUsersFragment.setArguments(bundle);
//                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                                transaction.replace(R.id.fragment_container, addUsersFragment).commit();
                                            }
                                        };
                                        addUser.start();
                                    }
                                };
                                getUser.start();
                            }
                        };
                        createGroup.start();
                    }
                }
            }
        });


    }

    private void getGroupMemberInfo(String groupID) {
        //Create ArrayList to store grpMemberIDs, HashMaps to store grpMemberNames and grpMemberImgs
        ArrayList<String> grpMemberIDs = new ArrayList<>();
        HashMap<String, String> grpMemberNames = new HashMap<>();
        HashMap<String, String> grpMemberImgs = new HashMap<>();
        Bundle bundle = new Bundle();

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
                                        bundle.putString("groupImage", String.valueOf(imageUri));
                                        bundle.putString("groupName", groupName);
                                        bundle.putString("groupId", groupID);

                                        //Fade Out Nav Bar
                                        Utils utils = new Utils(getContext());
                                        utils.fadeOut();

                                        //Go to IndivChatFragment
                                        IndivGroupFragment indivGroupFragment = new IndivGroupFragment();
                                        indivGroupFragment.setArguments(bundle);
                                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                        transaction.remove(CreateGroupFragment.this);
                                        transaction.add(R.id.fragment_container, indivGroupFragment, "openChat").commit();
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
}