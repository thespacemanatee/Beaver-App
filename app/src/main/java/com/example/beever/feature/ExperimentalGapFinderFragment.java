package com.example.beever.feature;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ExperimentalGapFinderFragment extends Fragment {

    EditText userId;
    Button userQueryButton;
    EditText groupId;
    Button groupQueryButton;
    TextView infoDisplay;

    public ExperimentalGapFinderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_experimental_gap_finder, container, false);
        userId = v.findViewById(R.id.edittext_experimental_user_id);
        userQueryButton = v.findViewById(R.id.button_experimental_user_id);
        groupId = v.findViewById(R.id.edittext_experimental_group_id);
        groupQueryButton = v.findViewById(R.id.button_experimental_group_id);
        infoDisplay = v.findViewById(R.id.textview_experimental_info);

        userQueryButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String queryUserId = userId.getText().toString();
                //Log.i("Test",queryUserId);
                if (queryUserId.equals("")) {
                    Toast.makeText(getActivity(),"Please enter a user id.",Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("users").document(queryUserId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String,Object> data = document.getData();
                                String setTextMsg = "Username: " + data.get("username") + "\n"
                                               + "Name: " + data.get("name") + "\n"
                                               + "Email: " + data.get("email") + "\n"
                                               + "Groups: " + data.get("groups").toString() + "\n"
                                               + "Dashboard groups: " + data.get("dashboard_grps").toString() + "\n";
                                //Log.i("Test2",setTextMsg);
                                infoDisplay.setText(setTextMsg);
                            } else {
                                Toast.makeText(getActivity(),"Error: user with specified id does not exist.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getActivity(),"Error: retrieval unsuccessful. Please try again.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

            }
        });

        groupQueryButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String queryGroupId = groupId.getText().toString();
                //Log.i("Test",queryUserId);
                if (queryGroupId.equals("")) {
                    Toast.makeText(getActivity(),"Please enter a group id.",Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("groups").document(queryGroupId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String,Object> data = document.getData();
                                String setTextMsg = "Name: " + data.get("name") + "\n"
                                        + "Member list: " + data.get("member_list").toString() + "\n"
                                        + "Colour: " + data.get("colour") + "\n"
                                        + "Display picture: " + data.get("display_picture") + "\n"
                                        + "Group events: " + data.get("group_events").toString() + "\n"
                                        + "Todo list: " + data.get("todo_list").toString() + "\n"
                                        + "Chat: " + data.get("chat") + "\n";
                                //Log.i("Test2",setTextMsg);
                                infoDisplay.setText(setTextMsg);
                            } else {
                                Toast.makeText(getActivity(),"Error: group with specified id does not exist.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getActivity(),"Error: retrieval unsuccessful. Please try again.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

            }
        });

        return v;
    }




}