package com.example.beever.database;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserEntry {

    // Object to represent user document. Only group id list, user events and todo lists are mutable

    // Store contents of user document as defined in specification
    private static final int DASHBOARD_GRPS = 6;

    private String username = null, name = null, email = null;
    private List<Object> groups, dashboard_grps;
    private Map<String,Object> user_events, todo_list;

    public UserEntry(){
        setGroups(null);
        setDashboard_grps(null);
        setUser_events(null);
        setTodo_list(null);
    }

    // Constructor for user document based on individual elements
    public UserEntry(String username, String name, String email, List<Object> groups,
                     List<Object> dashboard_grps, Map<String,Object> user_events, Map<String,Object> todo_list){
        setUsername(username);
        setName(name);
        setEmail(email);
        setGroups(groups);
        setDashboard_grps(dashboard_grps);
        setUser_events(user_events);
        setTodo_list(todo_list);
    }

    private void setUsername(String username){
        this.username = username;
    }

    private void setName(String name){
        this.name = name;
    }

    private void setEmail(String email){
        this.email = email;
    }

    private void setGroups(List<Object> groups){
        this.groups = groups==null? new ArrayList<Object>() : groups;
    }

    private void setDashboard_grps(List<Object> dashboard_grps){
        this.dashboard_grps = (dashboard_grps==null && dashboard_grps.size()==DASHBOARD_GRPS)? new ArrayList<Object>() : dashboard_grps;
        for (int i=0;i<DASHBOARD_GRPS;i++) this.dashboard_grps.add(null);
    }

    private void setUser_events(Map<String,Object> user_events){
        if (user_events!=null && user_events.containsKey("current") && user_events.containsKey("past") &&
        user_events.get("current") instanceof List && user_events.get("past") instanceof List) {
            this.user_events = user_events;
            return;
        }
        this.user_events = new HashMap<String,Object>();
        this.user_events.put("current",new ArrayList<Object>());
        this.user_events.put("past",new ArrayList<Object>());
    }

    private void setTodo_list(Map<String,Object> todo_list){
        if (todo_list!=null && todo_list.containsKey("current") && todo_list.containsKey("past") &&
                todo_list.get("current") instanceof List && todo_list.get("past") instanceof List) {
            this.todo_list = todo_list;
            return;
        }
        this.todo_list = new HashMap<String,Object>();
        this.todo_list.put("current",new ArrayList<Object>());
        this.todo_list.put("past",new ArrayList<Object>());
    }

    public String getUsername(){
        return username;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public List<Object> getGroups(){
        return groups;
    }

    public List<Object> getDashboard_grps(){
        return dashboard_grps;
    }

    public Map<String,Object> getUser_events(){return user_events;}

    public Map<String,Object> getTodo_list(){return todo_list;}

    // Add user to group, if user is not already in said group
    public void addGroupId(String groupId){
        if (!groups.contains(groupId)) groups.add(groupId);
    }

    // Remove user from group, if user is in said group
    public void removeGroupId(String groupId){
        groups.remove(groupId);
    }

    // Add or remove a user event or todo from the current or past event/todo list
    // isEvent: if true, add/remove to/from events list, else add/remove to/from todo list
    // isCurrent: if true, add/remove to/from current list, else add/remove to/from todo list
    // isAdd: if true, add the event/todo, else remove
    // eventOrTodo: the EventEntry/TodoEntry to add/remove - type matching will be asserted
    public void modifyEventOrTodo(boolean isEvent, boolean isCurrent, boolean isAdd, EventTodoEntry eventOrTodo){
        if (isEvent) assert eventOrTodo instanceof EventEntry;
        else assert eventOrTodo instanceof TodoEntry;
        Map<String,Object> selectCat = isEvent? user_events : todo_list;
        List<Object> selectList = isCurrent? (List<Object>) selectCat.get("current") : (List<Object>) selectCat.get("past");
        if (isAdd){
            selectList.add(eventOrTodo.getRepresentation());
        }
        else selectList.remove(eventOrTodo.getRepresentation());
    }

    // Get list of user events
    // getCurrent: if true, get current event
    // getPast: if true, get past event(so if you pass false to the last 2 elements, you would get
    // an empty ArrayList)
    public ArrayList<EventEntry> getUserEvents(boolean getCurrent, boolean getPast){
        ArrayList<EventEntry> ret = new ArrayList<EventEntry>();
        if (getCurrent){
            List<Object> currentEvents = (List<Object>) user_events.get("current");
            for (Object o:currentEvents) {
                ret.add(new EventEntry(o));
            }
        }
        if (getPast){
            List<Object> pastEvents = (List<Object>) user_events.get("past");
            for (Object o:pastEvents) {
                ret.add(new EventEntry(o));
            }
        }
        return ret;
    }

    // Again same, but for todo
    public ArrayList<TodoEntry> getUserTodo(boolean getCurrent, boolean getPast){
        ArrayList<TodoEntry> ret = new ArrayList<TodoEntry>();
        if (getCurrent){
            List<Object> currentTodo = (List<Object>) todo_list.get("current");
            for (Object o:currentTodo) {
                ret.add(new TodoEntry(o));
            }
        }
        if (getPast){
            List<Object> pastTodo = (List<Object>) todo_list.get("past");
            for (Object o:pastTodo) {
                ret.add(new TodoEntry(o));
            }
        }
        return ret;
    }

    // Assign a group_id to a dashboard groups slot
    public void assignDashboardGrp(int index, String group_id){
        if (index<0 || index>=6) return;
        dashboard_grps.set(index, group_id);
    }

    public String toString(){
        return "UserEntry({username=" + username + ",\n"
                + "name=" + name + ",\n"
                + "email=" + email + ",\n"
                + "groups=" + groups.toString() + ",\n"
                + "dashboard_grps=" + dashboard_grps.toString() + ",\n"
                + "user_events=" + user_events.toString() + ",\n"
                + "todo_list=" + todo_list.toString() + "})";
    }

    // TODO implement asynchronised method
    /*
    public static class getCurrentTodo extends AsyncTask<UserEntry,ArrayList<EventEntry>,ArrayList<EventEntry>>{

    }*/
}
