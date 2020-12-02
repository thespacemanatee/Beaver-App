package com.example.beever.feature;

import com.example.beever.database.EventEntry;

import java.util.Comparator;

public class DashboardEventComparator implements Comparator<EventEntry> {

    @Override
    public int compare(EventEntry o1, EventEntry o2) {
        return o1.getStart_time().compareTo(o2.getStart_time());
    }
}
