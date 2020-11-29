package com.example.beever.feature;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToDoFragment extends Fragment {

    public static final String TAG = "ToDoFragment";
    public static final String SPINNER = "Spinner Set-Up Successfully";
    public static final String RECYCLERVIEW = "RecyclerView Set-Up Successfully";
    public static final String FAB = "FAB Set-Up Successfully";
    public static final String ADD_TO_DO = "ADD_TO_DO";

    protected RecyclerView toDoRecyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected ToDoAdapter toDoAdapter;
    protected Spinner toDoSpinner;
    protected FloatingActionButton toDoAddButton;
    protected ExpandableListView toDoArchivedListView;
    protected ExpandableListAdapter toDoArchivedAdapter;

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID;
    private String groupID;

    protected List<String> ARCHIVED = new ArrayList<>();
    protected List<TodoEntry> archivedList = new ArrayList<>();
    protected HashMap<String, List<TodoEntry>> expandableListDetail = new HashMap<>();
    protected ArrayList<TodoEntry> toDoList = new ArrayList<>();
    protected List<String> projectList = new ArrayList<>();
    protected Map<String, Object> map = new HashMap<>();
    protected int scrollPosition = 0;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        FirebaseUser fUser = fAuth.getCurrentUser();
        userID = fUser.getUid();

        populateProjectList();
        if (!projectList.isEmpty()) {
            groupID = projectList.get(0);
        }

        View rootView = layoutInflater.inflate(R.layout.fragment_to_do, viewGroup, false);
        rootView.setTag(TAG);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("To-Do");

        // setting Spinner to select Project
        toDoSpinner = rootView.findViewById(R.id.toDoSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, projectList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        toDoSpinner.setAdapter(adapter);
        toDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO: get the to do list from firebase according to the project
                groupID = projectList.get(position);
                Log.d("GROUP ID", groupID);
                GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupID, 5000 ) {
                    @Override
                    public void onPostExecute() {
                        if (isSuccessful()) {
                            toDoList = getResult().getGroupTodo(true, false);
                            Log.d("TODO LIST", String.valueOf(toDoList));
                            archivedList = getResult().getGroupTodo(false, true);
                            Log.d("ARCHIVED LIST", String.valueOf(archivedList));
                        } else {
                            Toast.makeText(getContext(), "To Do List Not Found", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                getGroupEntry.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Log.d(TAG, SPINNER);

        // set Linear Layout for RecyclerView To Do List
        toDoRecyclerView = rootView.findViewById(R.id.toDoRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        if (toDoRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) toDoRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        toDoRecyclerView.setLayoutManager(layoutManager);
        toDoRecyclerView.setItemAnimator(new DefaultItemAnimator());
        toDoRecyclerView.scrollToPosition(scrollPosition);

        // set toDoAdapter for RecyclerView
        toDoAdapter = new ToDoAdapter(toDoList, getFragmentManager(), groupID);
        toDoRecyclerView.setAdapter(toDoAdapter);
        Log.d(TAG, RECYCLERVIEW);

        // set Archived to dos for Expandable View
        toDoArchivedListView = rootView.findViewById(R.id.toDoArchivedListView);
        toDoArchivedAdapter = new ExpandableListAdapter(this.getContext(), ARCHIVED, expandableListDetail);
        toDoArchivedListView.setAdapter(toDoArchivedAdapter);
        toDoArchivedListView.setOnGroupExpandListener(groupPosition -> Toast.makeText(getContext(), "Archived Expanded.", Toast.LENGTH_SHORT).show());

        // set To Do Form for FloatingActionButton
        toDoAddButton = rootView.findViewById(R.id.toDoAddButton);
        toDoAddButton.setOnClickListener(v -> {
            if (groupID != null) {
                ToDoDialogFragment toDoDialogFragment = new ToDoDialogFragment(groupID);
                toDoDialogFragment.show(getFragmentManager(), ADD_TO_DO);
            } else {
                Toast.makeText(getContext(), "No Group Available", Toast.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, FAB);

        return rootView;
    }

    private void populateProjectList() {
        UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(userID, 5000) {
            @Override
            public void onPostExecute() {
                if (isSuccessful()) {
                    List<Object> groups = getResult().getGroups();
                    for (Object group : groups) {
                        Log.d("PROJECT LIST", (String) group);
                        projectList.add((String) group);
                    }
                } else {
                    Toast.makeText(getContext(), "Groups Not Found", Toast.LENGTH_LONG).show();
                }
            }
        };

        getUserEntry.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "ON START");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ON PAUSE");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "ON STOP");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "ON DESTROYVIEW");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ON DESTROY");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "ON DETACH");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ON RESUME");
    }
}