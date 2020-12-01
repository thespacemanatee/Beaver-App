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
import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class GapFinderFragment extends Fragment {

    EditText userId;
    Button userQueryButton;
    EditText groupId0;
    EditText groupId1;
    EditText groupId2;
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
        groupId0 = v.findViewById(R.id.edittext_experimental_group_id0);
        groupId1 = v.findViewById(R.id.edittext_experimental_group_id1);
        groupId2 = v.findViewById(R.id.edittext_experimental_group_id2);
        groupQueryButton = v.findViewById(R.id.button_experimental_group_id);
        infoDisplay = v.findViewById(R.id.textview_experimental_info);

        userQueryButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String queryUserId = userId.getText().toString();
                //Log.i("Test",queryUserId);
                if (queryUserId.equals("")) {
                    Toast.makeText(getActivity(),"Please enter a valid id.",Toast.LENGTH_SHORT).show();
                    return;
                }
                UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(queryUserId,5000){
                    public void onPostExecute(){
                        if (!isSuccessful()){
                            infoDisplay.setText("Failed0");
                            return;
                        }
                        String ret = "";
                        for (EventEntry e:getResult().getUserEvents(true,true)){
                            ret += "Name=" + e.getName() + '\n';
                            ret += "\tDescription=" + e.getDescription() + '\n';
                            ret += "\tStart time=\n";
                            Timestamp start_time = e.getStart_time();
                            ret += "\t\tYear=" + start_time.toDate().getYear() + '\n';
                            ret += "\t\tMonth=" + start_time.toDate().getMonth() + '\n';
                            ret += "\t\tDay=" + start_time.toDate().getDay() + '\n';
                            ret += "\tEnd time=\n";
                            start_time = e.getEnd_time();
                            ret += "\t\tYear=" + start_time.toDate().getYear() + '\n';
                            ret += "\t\tMonth=" + start_time.toDate().getMonth() + '\n';
                            ret += "\t\tDay=" + start_time.toDate().getDay() + '\n';
                        }
                        infoDisplay.setText(ret);
                    }
                };
                getUserEntry.start();
            }
        });

        groupQueryButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String queryGroupId = "0";
                String queryYear = groupId0.getText().toString();
                String queryMonth = groupId1.getText().toString();
                String queryDay = groupId2.getText().toString();
                if (queryYear.equals("") | queryMonth.equals("") | queryDay.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a valid timing.", Toast.LENGTH_SHORT).show();
                    return;
                }
                GapFinderAlgorithm gapFinder = new GapFinderAlgorithm(queryGroupId,10000,
                        Integer.parseInt(queryYear),Integer.parseInt(queryMonth),Integer.parseInt(queryDay),60){
                    public void onPostExecute(){
                        if (!isSuccessful()){
                            infoDisplay.setText("Failed0");
                            return;
                        }
                        String ret = "";
                        for (ArrayList<Timestamp> t: getResult()){
                            ret += t.get(0).toDate().toString() + '\n';
                            ret += '\t' + t.get(1).toDate().toString() + '\n';
                        }
                        infoDisplay.setText(ret);
                    }
                };
                gapFinder.getGaps();

            }
        });

        return v;
    }
}