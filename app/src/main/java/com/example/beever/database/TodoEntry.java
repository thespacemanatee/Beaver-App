package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent todo entries in the Firestore database.
 * This class is partly for auto-generation via UserEntry.class and GroupEntry.class.
 *
 * Todo entries follow this contract, the TodoEntry contract, in Firestore:
 * - [Todo number in array]: Map<Object>
 *    - name: String
 *    - description: String
 *    - assigned_to: String
 *    - group_id_source: String (nullable)
 *    - deadline: Timestamp
 *
 * To create an instance of this class manually, use only the 3rd constructor.
 **/
public class TodoEntry extends EventTodoEntry {

    private String name = null, description = null, assigned_to = null, group_id_source = null;
    private Timestamp deadline = null;

    /**
     * No-arg constructor for debugging
     */
    public TodoEntry(){}

    /**
     * Constructor to create an TodoEntry from a Map<String,Object>, usually to extract
     * TodoEntry objects from existing UserEntry/GroupEntry objects. If passed Object is not
     * a Map, behaviour is mostly the same as the no-arg constructor.
     * @param o object to convert to EventEntry, must be a Map
     */
    public TodoEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("TodoEntry creation","Passed object is not a Map");
            return;
        }
        Map<String,Object> map = (Map<String,Object>) o;
        setName((String) map.get("name"));
        setDescription((String) map.get("description"));
        setAssigned_to((String) map.get("assigned_to"));
        setGroup_id_source((String) map.get("group_id_source"));
        setDeadline((Timestamp) map.get("deadline"));
    }

    /**
     * Constructor for manually generating TodoEntry objects.
     * @param name todo name
     * @param description todo description
     * @param assigned_to user ID of user assigned the todo
     * @param deadline time by which user agrees to complete task
     * @param group_id_source group ID of group containing this todo, if this is a group todo, or null otherwise
     */
    public TodoEntry(String name, String description, String assigned_to, Timestamp deadline, String group_id_source){
        setName(name);
        setDescription(description);
        setAssigned_to(assigned_to);
        setDeadline(deadline);
        setGroup_id_source(group_id_source);
    }

    // Setters

    public void setName(String name) {this.name = name;}

    public void setDescription(String description) {this.description = description;}

    public void setAssigned_to(String assigned_to) {this.assigned_to = assigned_to;}

    public void setGroup_id_source(String group_id_source) {this.group_id_source = group_id_source;}

    public void setDeadline(Timestamp deadline) {this.deadline = deadline;}

    // Getters

    public String getName() {return name;}

    public String getDescription() {return description;}

    public String getAssigned_to() {return assigned_to;}

    public String getGroup_id_source() {return group_id_source;}

    public Timestamp getDeadline() {return deadline;}

    // Miscellaneous functions

    /**
     * Check whether this is a group todo.
     * @return boolean for whether event is a group todo
     */
    public boolean isGroupEntry(){
        return group_id_source!=null;
    }

    /**
     * Get id of todo source, whether user or group.
     * @return ID of todo creator
     */
    public String getSource(){
        return isGroupEntry()? group_id_source : assigned_to;
    }

    /**
     * Get equivalent Map object representation which obeys TodoEntry contract,
     * for addition to UserEntry/GroupEntry
     * @return Map object representation
     */
    public Map<String,Object> getRepresentation(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name",name);
        map.put("description",description);
        map.put("assigned_to",assigned_to);
        map.put("deadline",deadline);
        map.put("group_id_source",group_id_source);
        return map;
    }

    /**
     * Check if this TodoEntry object equals another object
     * @param o object to check equality with
     * @return boolean for whether this object equals the other object
     */
    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof TodoEntry)){return false;}
        TodoEntry other = (TodoEntry) o;
        return getRepresentation().equals(other.getRepresentation());
    }

    /**
     * Get string representation of this TodoEntry
     * @return string representation
     */
    public String toString(){
        return "TodoEntry({\n\tname=" + name + ",\n"
                + "\tdescription=" + description + ",\n"
                + "\tassigned_to=" + assigned_to + ",\n"
                + "\tgroup_id_source=" + group_id_source + ",\n"
                + "\tdeadline==" + deadline.toString() + "\n})";
    }
}
