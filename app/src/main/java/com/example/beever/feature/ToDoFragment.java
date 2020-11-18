package com.example.beever.feature;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;

import java.util.Date;
import java.util.List;

public class ToDoFragment extends Fragment {

    private RecyclerView toDoRecyclerView;
    private ToDoAdapter toDoAdapter;

    private List<ToDoModel> toDoModelList;
    ViewGroup root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = (ViewGroup) inflater.inflate(R.layout.fragment_to_do, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("To-Do");

        toDoRecyclerView = root.findViewById(R.id.toDoRecyclerView);
        toDoRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        toDoAdapter = new ToDoAdapter(root.getContext());
        toDoRecyclerView.setAdapter(toDoAdapter);

        return root;
    }

    static class ToDoAdapter extends RecyclerView.ViewHolder {
        private List<ToDoModel> toDoList;
        private Context context;

        public ToDoAdapter (Context context) {
            this.context = context;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_to_do_task_, parent, false);

            return new ViewHolder(itemView);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            ToDoModel item = toDoList.get(position);
            holder.toDo.setText(item.getItemName());
            holder.toDo.setChecked(toBoolean(item.getArchived()));
        }

        private boolean toBoolean(int n) {
            return n != 0;
        }

        public int getItemCount() {
            return toDoList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox toDo;

            ViewHolder(View view) {
                super(view);
                toDo = view.findViewById(R.id.toDoCheckbox);
            }
        }

    }

    class ToDoModel {
        private int id, archived, overdue;
        private String itemName, description;
        private Date dueDate;
        // TODO: 17/11/2020 assignedTo: user(obj)


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getArchived() {
            return archived;
        }

        public void setArchived(int archived) {
            this.archived = archived;
        }

        public int getOverdue() {
            return overdue;
        }

        public void setOverdue(int overdue) {
            this.overdue = overdue;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }
    }
}