package com.example.beever.feature;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.ThreadAsyncTaskContainer;
import com.example.beever.database.UserEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
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
                Handler e = new Handler();
                UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(queryUserId,50000){
                    public void onPostExecute(){
                        infoDisplay.setText(getResult().toString());
                    }
                };
                getUserEntry.start();
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
                                GroupEntry groupEntry = document.toObject(GroupEntry.class);
                                infoDisplay.setText(groupEntry.getGroupEvents(true,true).toString());
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