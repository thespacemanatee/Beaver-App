package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class EventEntry implements EventTodoEntry {
    private String name = null, description = null;
    private Timestamp start_time = null, end_time = null;

    public EventEntry(){}

    public EventEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("EventEntry creation","Passed object is not a Map");
            return;
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

    private void setName(String name){
        this.name = name;
    }

    private void setDescription(String description){
        this.description = description;
    }

    private void setStart_time(Timestamp start_time){
        this.start_time = start_time;
    }

    private void setEnd_time(Timestamp end_time){
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

    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof EventEntry)){return false;}
        EventEntry other = (EventEntry) o;
        return name.equals(other.name) && description.equals(other.description) && start_time.equals(other.start_time) && end_time.equals(other.end_time);
    }

    // Print to string, mostly for debugging
    public String toString(){
        return "EventEntry({name=" + name + ",\n"
                + "description=" + description + ",\n"
                + "start_time=" + start_time.toString() + ",\n"
                + "end_time=" + end_time.toString() + "})";
    }

}
