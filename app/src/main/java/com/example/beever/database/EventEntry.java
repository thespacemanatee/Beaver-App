package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent event entries in the Firestore database.
 * This class is partly for auto-generation via UserEntry.class and GroupEntry.class.
 *
 * Event entries follow this contract, the EventEntry contract, in Firestore:
 * - [Event number in array]: Map<Object>
 *    - name: String
 *    - description: String
 *    - user_id_source: String (nullable), either this or group_id_source must be null
 *    - group_id_source: String (nullable), either this or group_id_source must be null
 *    - start_time: Timestamp
 *    - end_time: Timestamp
 *
 * To create an instance of this class manually, use only the 3rd constructor.
 **/
public class EventEntry extends EventTodoEntry {

    private String name = null, description = null, user_id_source = null, group_id_source = null;
    private Timestamp start_time = null, end_time = null;

    /**
     * No-arg constructor for debugging
     */
    public EventEntry(){}

    /**
     * Constructor to create an EventEntry from a Map<String,Object>, usually to extract
     * EventEntry objects from existing UserEntry/GroupEntry objects. If passed Object is not
     * a Map, behaviour is mostly the same as the no-arg constructor.
     * @param o object to convert to EventEntry, must be a Map
     */
    public EventEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("EventEntry creation","Passed object is not a Map");
            return;
        }
        Map<String,Object> map = (Map<String,Object>) o;
        assert (map.get("user_id_source")==null && map.get("group_id_source") instanceof String)
                || (map.get("user_id_source") instanceof String && map.get("group_id_source")==null);
        setName((String) map.get("name"));
        setDescription((String) map.get("description"));
        setUser_id_source((String) map.get("user_id_source"));
        setGroup_id_source((String) map.get("group_id_source"));
        setStart_time((Timestamp) map.get("start_time"));
        setEnd_time((Timestamp) map.get("end_time"));
    }

    /**
     * Constructor for manually generating EventEntry
     * @param name event name
     * @param description event name
     * @param user_id_source user id of creating user, if this is a personal event, or null
     * @param group_id_source group id of creating group, if this is a group event, or null
     * @param start_time starting time
     * @param end_time ending time
     */
    public EventEntry(String name, String description, String user_id_source, String group_id_source, Timestamp start_time, Timestamp end_time){
        assert (user_id_source==null && group_id_source instanceof String) || (user_id_source instanceof String && group_id_source==null);
        setName(name);
        setDescription(description);
        setStart_time(start_time);
        setEnd_time(end_time);
        setUser_id_source(user_id_source);
        setGroup_id_source(group_id_source);
    }

    // Setters

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Set user id source. Note that as per EventEntry contract, if user_id_source is non-null,
     * then group_id_source must be null and hence will be set as such.
     * @param user_id_source user ID of user source of event
     */
    public void setUser_id_source(String user_id_source){
        this.user_id_source = user_id_source;
        if (user_id_source!=null) group_id_source = null;
    }

    /**
     * Set user id source. Note that as per EventEntry contract, if group_id_source is non-null,
     * then user_id_source must be null and hence will be set as such.
     * @param group_id_source group ID of group source of event
     */
    public void setGroup_id_source(String group_id_source){
        this.group_id_source = group_id_source;
        if (group_id_source!=null) user_id_source = null;
    }

    public void setStart_time(Timestamp start_time){
        this.start_time = start_time;
    }

    public void setEnd_time(Timestamp end_time){
        this.end_time = end_time;
    }

    // Getters

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getUser_id_source(){
        return user_id_source;
    }

    public String getGroup_id_source(){
        return group_id_source;
    }

    public Timestamp getStart_time(){
        return start_time;
    }

    public Timestamp getEnd_time(){
        return end_time;
    }

    // Miscellaneous functions

    /**
     * Check whether this is a group event.
     * @return boolean for whether event is a group event
     */
    public boolean isGroupEntry(){
        return user_id_source==null;
    }

    /**
     * Get id of event source, whether user or group. This assumes that the EventEntry
     * contract is fully obeyed, which may not be the case for objects created using
     * the first constructor.
     * @return ID of event creator
     */
    public String getSource(){
        return isGroupEntry()? group_id_source : user_id_source;
    }

    /**
     * Get equivalent Map object representation which obeys EventEntry contract,
     * for addition to UserEntry/GroupEntry
     * @return Map object representation
     */
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

    /**
     * Check if this EventEntry object equals another object
     * @param o object to check equality with
     * @return boolean for whether this object equals the other object
     */
    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof EventEntry)){return false;}
        EventEntry other = (EventEntry) o;
        return getRepresentation().equals(other.getRepresentation());
    }

    /**
     * Get string representation of this EventEntry
     * @return string representation
     */
    public String toString(){
        return "EventEntry({\n\tname=" + name + ",\n"
                + "\tdescription=" + description + ",\n"
                + "\tuser_id_source=" + user_id_source + ",\n"
                + "\tgroup_id_source=" + group_id_source + ",\n"
                + "\tstart_time=" + start_time.toString() + ",\n"
                + "\tend_time=" + end_time.toString() + ",\n})";
    }

}
