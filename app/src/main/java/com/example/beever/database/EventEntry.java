package com.example.beever.database;

import android.util.Log;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class EventEntry {
    private String name = null, description = null;
    private Timestamp start_time = null, end_time = null;

    public EventEntry(){}

    public EventEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("Event creation","Passed object is not a Map");
        }
        Map<String,Object> map = (Map<String,Object>) o;
        setName((String) map.get("name"));
        setDescription((String) map.get("description"));
        setStart_time((Timestamp) map.get("start_time"));
        setEnd_time((Timestamp) map.get("end_time"));
    }

    public EventEntry(String name, String description, Timestamp start_time, Timestamp end_time){
        this.name = name;
        this.description = description;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setStart_time(Timestamp start_time){
        this.start_time = start_time;
    }

    public void setEnd_time(Timestamp end_time){
        this.end_time = end_time;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public Timestamp getStart_time(){
        return start_time;
    }

    public Timestamp getEnd_time(){
        return end_time;
    }

    public Map<String, Object> getRepresentation(){
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", name);
        ret.put("description", description);
        ret.put("start_time", start_time);
        ret.put("end_time", end_time);
        return ret;
    }

    public String toString(){
        return "Name: " + name + "\n"
                + "Description: " + description + "\n"
                + "Start time: " + start_time.toString() + "\n"
                + "End time: " + end_time.toString() + "\n";
    }

}
