package com.example.beever.feature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.beever.R;
import com.example.beever.database.TodoEntry;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    protected Context context;
    protected List<String> expandableListTitle;
    protected HashMap<String, List<TodoEntry>> expandableListDetail;

    public ExpandableListAdapter(Context context, List<String> expandableListTitle, HashMap<String, List<TodoEntry>> expandableListDetail) {
        this.context = context;
        this.expandableListDetail = expandableListDetail;
        this.expandableListTitle = expandableListTitle;
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (this.expandableListDetail.get(this.expandableListTitle.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.expandableListTitle.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (this.expandableListDetail.get(this.expandableListTitle.get(groupPosition))).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_group, null);
        }

        TextView toDoArchivedListText = convertView.findViewById(R.id.toDoArchivedListText);
        toDoArchivedListText.setText(listTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final TodoEntry todoEntry = (TodoEntry) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_to_do_task_, null);
        }

        ConstraintLayout toDoTaskView = convertView.findViewById(R.id.toDoTaskView);
        TextView toDoTaskContent = toDoTaskView.findViewById(R.id.toDoTaskContent);
        TextView toDoAssignedTo = toDoTaskView.findViewById(R.id.toDoAssignedTo);
        TextView toDoDeadline = toDoTaskView.findViewById(R.id.toDoDeadline);

        Timestamp deadline = todoEntry.getDeadline();
        SimpleDateFormat sf = new SimpleDateFormat("dd-MM", Locale.getDefault());
        sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String deadlineStr = sf.format(deadline.toDate());

        toDoTaskContent.setText(todoEntry.getName());
        toDoAssignedTo.setText(todoEntry.getAssigned_to());
        toDoDeadline.setText(deadlineStr);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
