package com.example.beever.feature;

import com.example.beever.database.TodoEntry;

import java.util.Comparator;


/**
 * ToDoComparator : Compares TodoEntries by deadline so that we can sort by deadline
 */
public class ToDoComparator implements Comparator<TodoEntry> {

    @Override
    public int compare(TodoEntry o1, TodoEntry o2) {
        return o1.getDeadline().compareTo(o2.getDeadline());
    }
}
