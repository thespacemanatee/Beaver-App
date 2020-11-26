package com.example.beever.database;

import java.util.Map;

public abstract class EventTodoEntry {
    public abstract Map<String, Object> getRepresentation();

    public abstract boolean isGroupEntry();

    public abstract String getSource();
}
