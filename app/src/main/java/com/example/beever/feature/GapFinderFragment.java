package com.example.beever.feature;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;

public class GapFinderFragment extends Fragment {

    EditText userId;
    Button userQueryButton;
    EditText groupId;
    Button groupQueryButton;
    TextView infoDisplay;

    public GapFinderFragment() {
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
                UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(queryUserId,5000){
                    public void onPostExecute(){
                        if (!isSuccessful()){
                            infoDisplay.setText("Failed0");
                            return;
                        }
                        UserEntry.GetUserRelevantTodos getUserRelevantTodos = new UserEntry.GetUserRelevantTodos(getResult(),5000,true,true,queryUserId) {
                            @Override
                            public void onPostExecute() {
                                infoDisplay.setText(isSuccessful()? getResult().toString():"Failed1");
                            }
                        };
                        getUserRelevantTodos.start();
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
                    Toast.makeText(getActivity(), "Please enter a group id.", Toast.LENGTH_SHORT).show();
                    return;
                }
                GroupEntry.GroupEntryListener groupEntryListener = new GroupEntry.GroupEntryListener(queryGroupId,5000) {
                    public void onPreListening(){
                        infoDisplay.setText(exists()? "Initial retrieval success" : "InitFail");
                    }

                    @Override
                    public void onListenerUpdate() {
                        infoDisplay.setText(exists()? getStateChange().toString() : "Failed");
                    }
                };
                groupEntryListener.start();

            }
        });

        return v;
    }
}