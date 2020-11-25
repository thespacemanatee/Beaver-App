package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TodoEntry implements EventTodoEntry {
    private String name = null, description = null, assigned_to = null;
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
    }

    public TodoEntry(String name, String description, String assigned_to, Timestamp deadline){
        setName(name);
        setDescription(description);
        setAssigned_to(assigned_to);
        setDeadline(deadline);
    }

    private void setName(String name) {this.name = name;}

    private void setDescription(String description) {this.description = description;}

    private void setAssigned_to(String assigned_to) {this.assigned_to = assigned_to;}

    private void setDeadline(Timestamp deadline) {this.deadline = deadline;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public String getAssigned_to() {return assigned_to;}

    public Timestamp getDeadline() {return deadline;}

    // Get corresponding object representation for storage in database
    public Map<String,Object> getRepresentation(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name",name);
        map.put("description",description);
        map.put("assigned_to",assigned_to);
        map.put("deadline",deadline);
        return map;
    }

    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof TodoEntry)){return false;}
        TodoEntry other = (TodoEntry) o;
        return name.equals(other.getName()) && description.equals(other.getDescription()) && assigned_to.equals(other.getAssigned_to()) && deadline.equals(other.getDeadline());
    }

    public String toString(){
        return "TodoEntry({name=" + name + ",\n"
                + "description=" + description + ",\n"
                + "assigned_to=" + assigned_to + ",\n"
                + "deadline=" + deadline.toString() + "})";
    }
}
