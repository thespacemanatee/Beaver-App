package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class EventEntry extends EventTodoEntry {
    private String name = null, description = null, user_id_source = null, group_id_source = null;
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
        setUser_id_source((String) map.get("user_id_source"));
        setGroup_id_source((String) map.get("group_id_source"));
    }

    public EventEntry(String name, String description, Timestamp start_time, Timestamp end_time, String user_id_source, String group_id_source){
        assert (user_id_source==null && group_id_source instanceof String) || (user_id_source instanceof String && group_id_source==null);
        setName(name);
        setDescription(description);
        setStart_time(start_time);
        setEnd_time(end_time);
        setUser_id_source(user_id_source);
        setGroup_id_source(group_id_source);
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

    private void setUser_id_source(String user_id_source){
        this.user_id_source = user_id_source;
    }

    private void setGroup_id_source(String group_id_source){
        this.group_id_source = group_id_source;
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

    public String getUser_id_source(){
        return user_id_source;
    }

    public String getGroup_id_source(){
        return group_id_source;
    }

    public boolean isGroupEntry(){
        return user_id_source==null;
    }

    public String getSource(){
        return isGroupEntry()? group_id_source : user_id_source;
    }

    public Map<String, Object> getRepresentation(){
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", name);
        ret.put("description", description);
        ret.put("start_time", start_time);
        ret.put("end_time", end_time);
        ret.put("user_id_source", user_id_source);
        ret.put("group_id_source", group_id_source);
        return ret;
    }

    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof EventEntry)){return false;}
        EventEntry other = (EventEntry) o;
        return name.equals(other.getName()) && description.equals(other.getDescription())
                && start_time.equals(other.getStart_time()) && end_time.equals(other.getEnd_time())
                && user_id_source.equals(other.getUser_id_source()) && group_id_source.equals(other.getGroup_id_source());
    }

    // Print to string, mostly for debugging
    public String toString(){
        return "EventEntry({\n\tname=" + name + ",\n"
                + "\tdescription=" + description + ",\n"
                + "\tstart_time=" + start_time.toString() + ",\n"
                + "\tend_time=" + end_time.toString() + ",\n"
                + "\tuser_id_source=" + user_id_source + ",\n"
                + "\tgroup_id_source=" + group_id_source + "\n})";
    }

}
