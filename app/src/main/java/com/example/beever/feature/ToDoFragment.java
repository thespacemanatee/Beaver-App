package com.example.beever.feature;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToDoFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    public static final String TAG = "ToDoFragment";
    public static final String SPINNER = "Spinner Set-Up Successfully";
    public static final String RECYCLERVIEW = "RecyclerView Set-Up Successfully";
    public static final String FAB = "FAB Set-Up Successfully";

    protected RecyclerView toDoRecyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected ToDoAdapter toDoAdapter;
    protected Spinner toDoSpinner;
    protected FloatingActionButton toDoAddButton;
    protected ExpandableListView toDoArchivedListView;
    protected ExpandableListAdapter toDoArchivedAdapter;
    protected List<String> ARCHIVED = new ArrayList<>();
    protected HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    protected ArrayList<String> toDoList = new ArrayList<>();
    protected ArrayList<String> projectList = new ArrayList<>();
    protected int scrollPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialise to-do and project list from firebase
        initToDoList();
        initProjectList();
        initArchivedList();
    }

    /**
     * initialise to do list with items (of type String) from firebase
     */
    private void initToDoList() {
        // TODO: get to do list from firebase
        for (int i = 0; i < 20; i++) {
            toDoList.add("This is task number " + String.valueOf(i) + "\nThis is the DueDate");
        }
    }

    /**
     * initialise project list with items (of type String) from firebase
     */
    private void initProjectList() {
        // TODO: get project list from firebase
        projectList.add("50.001 1D");
        projectList.add("50.002 1D");
        projectList.add("50.004 2D");
    }

    /**
     * initialise archived list with items (of type String) from firebase
     */
    private void initArchivedList() {
        // TODO: get archived list from firebase
        List<String> archivedList = new ArrayList<>();
        ARCHIVED.add("Archived");
        archivedList.add("Hello World");
        archivedList.add("This is Done");
        archivedList.add("This is Archived");
        for (int i = 0; i < 10; i++) {
            archivedList.add("Done Task " + i);
        }
        expandableListDetail.put(ARCHIVED.get(0), archivedList);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View rootView = layoutInflater.inflate(R.layout.fragment_to_do, viewGroup, false);
        rootView.setTag(TAG);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("To-Do");

        // setting Spinner to select Project
        toDoSpinner = rootView.findViewById(R.id.toDoSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, projectList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        toDoSpinner.setAdapter(adapter);
        toDoSpinner.setOnItemSelectedListener(this);
        Log.d(TAG, SPINNER);

        toDoRecyclerView = rootView.findViewById(R.id.toDoRecyclerView);

        // set Linear Layout for RecyclerView To Do List
        layoutManager = new LinearLayoutManager(getActivity());
        if (toDoRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) toDoRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        toDoRecyclerView.setLayoutManager(layoutManager);
        toDoRecyclerView.scrollToPosition(scrollPosition);

        toDoAdapter = new ToDoAdapter(toDoList);
        // set toDoAdapter for RecyclerView
        toDoRecyclerView.setAdapter(toDoAdapter);
        Log.d(TAG, RECYCLERVIEW);

        // set Archived to dos for Expandable View
        toDoArchivedListView = rootView.findViewById(R.id.toDoArchivedListView);
        toDoArchivedAdapter = new ExpandableListAdapter(this.getContext(), ARCHIVED, expandableListDetail);
        toDoArchivedListView.setAdapter(toDoArchivedAdapter);
        toDoArchivedListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getContext(), "Archived Expanded.", Toast.LENGTH_SHORT);
            }
        });

        // set To Do Form for FloatingActionButton
        toDoAddButton = rootView.findViewById(R.id.toDoAddButton);
        toDoAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Add TO DO", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, FAB);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerItem = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + spinnerItem, Toast.LENGTH_SHORT).show();
        // TODO: get the to do list from firebase according to the project
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}