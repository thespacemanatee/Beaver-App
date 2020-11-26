package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TodoEntry extends EventTodoEntry {
    private String name = null, description = null, assigned_to = null, group_id_source = null;
    private Timestamp deadline = null;

    public TodoEntry(){}

    public TodoEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("TodoEntry creation","Passed object is not a Map");
            return;
        }
        Map<String,Object> map = (Map<String,Object>) o;
        setName((String) map.get("name"));
        setDescription((String) map.get("description"));
        setAssigned_to((String) map.get("assigned_to"));
        setDeadline((Timestamp) map.get("deadline"));
        setGroup_id_source((String) map.get("group_id_source"));
    }

    public TodoEntry(String name, String description, String assigned_to, Timestamp deadline, String group_id_source){
        setName(name);
        setDescription(description);
        setAssigned_to(assigned_to);
        setDeadline(deadline);
        setGroup_id_source(group_id_source);
    }

    private void setName(String name) {this.name = name;}

    private void setDescription(String description) {this.description = description;}

    private void setAssigned_to(String assigned_to) {this.assigned_to = assigned_to;}

    private void setDeadline(Timestamp deadline) {this.deadline = deadline;}

    private void setGroup_id_source(String group_id_source) {this.group_id_source = group_id_source;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public String getAssigned_to() {return assigned_to;}

    public Timestamp getDeadline() {return deadline;}

    public String getGroup_id_source() {return group_id_source;}

    // Get corresponding object representation for storage in database
    public Map<String,Object> getRepresentation(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name",name);
        map.put("description",description);
        map.put("assigned_to",assigned_to);
        map.put("deadline",deadline);
        map.put("group_id_source",group_id_source);
        return map;
    }

    public boolean isGroupEntry(){
        return group_id_source!=null;
    }

    public String getSource(){
        return isGroupEntry()? group_id_source : assigned_to;
    }

    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof TodoEntry)){return false;}
        TodoEntry other = (TodoEntry) o;
        return name.equals(other.getName()) && description.equals(other.getDescription()) && assigned_to.equals(other.getAssigned_to())
                && deadline.equals(other.getDeadline()) && group_id_source.equals(other.getGroup_id_source());
    }

    public String toString(){
        return "TodoEntry({\n\tname=" + name + ",\n"
                + "\tdescription=" + description + ",\n"
                + "\tassigned_to=" + assigned_to + ",\n"
                + "\tdeadline=" + deadline.toString() + ",\n"
                + "\tgroup_id_source=" + group_id_source + "\n})";
    }
}
