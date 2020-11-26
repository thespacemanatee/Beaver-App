package com.example.beever.database;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
        if (dashboard_grps!=null && dashboard_grps.size()==DASHBOARD_GRPS){
            this.dashboard_grps = dashboard_grps;
            return;
        }
        this.dashboard_grps = new ArrayList<Object>();
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

    public abstract static class GetUserEntry extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserEntry";
        private static final int SLEEP_INCREMENT = 10;
        private UserEntry result = null;

        private String userId;
        private Integer timeout;

        public GetUserEntry(String userId,Integer timeout){
            this.userId = userId;
            this.timeout = timeout;
        }

        // params: String userId, Integer timeout
        public void runMainBody() {

            UserEntry user[] = {null};
            final boolean[] finish = {false};

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            user[0] = document.toObject(UserEntry.class);
                            finish[0] = true;

                        } else {
                            Log.d(LOG_NAME, "User ID not found");
                            finish[0] = true;
                        }
                    } else {
                        Log.d(LOG_NAME, "Retrieval error");
                        finish[0] = true;
                    }
                }
            });

            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (now < end) {
                try {
                    Thread.sleep(SLEEP_INCREMENT);
                } catch (InterruptedException e) {
                }
                now = System.currentTimeMillis();
                if (finish[0]) {
                    break;
                }
            }
            if (!finish[0]) {
                Log.d(LOG_NAME, "Timeout");
            }

            result = user[0];
            return;
        }

        public UserEntry getResult(){
            return result;
        }

        public boolean isSuccessful(){
            return result!=null;
        }

        public abstract void onPostExecute();
    }

    public abstract static class SetUserEntry extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.SetUserEntry";
        private static final int SLEEP_INCREMENT = 10;
        private Boolean result = null;

        private UserEntry userEntry;
        private String userId;
        private Integer timeout;

        public SetUserEntry(UserEntry userEntry, String userId, Integer timeout){
            this.userEntry = userEntry;
            this.userId = userId;
            this.timeout = timeout;
        }

        public void runMainBody(){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .set(userEntry)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            result = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(LOG_NAME, "Update error");
                            result = false;
                        }
                    });
            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (now < end) {
                try {
                    Thread.sleep(SLEEP_INCREMENT);
                } catch (InterruptedException e) {
                }
                now = System.currentTimeMillis();
                if (result!=null) {
                    break;
                }
            }
            if (result!=null) {
                Log.d(LOG_NAME, "Timeout");
                result = false;
            }
            return;
        }
        public boolean isSuccessful(){
            return result!=null && result;
        }

        public abstract void onPostExecute();
    }

    public abstract static class GetUserRelevantEvents extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserRelevantEvents";
        private static final int SLEEP_INCREMENT = 10;
        private ArrayList<EventEntry> result = null;

        private UserEntry userEntry;
        private Integer timeout;
        private boolean getCurrent, getPast;

        public GetUserRelevantEvents(UserEntry userEntry,Integer timeout,boolean getCurrent,boolean getPast){
            this.userEntry = userEntry;
            this.timeout = timeout;
            this.getCurrent = getCurrent;
            this.getPast = getPast;
        }

        @SuppressLint("LongLogTag")
        public void runMainBody() {

            ArrayList<GroupEntry> groupResults = new ArrayList<GroupEntry>();
            LinkedList<GroupEntry.GetGroupEntry> groupEntryGetters = new LinkedList<GroupEntry.GetGroupEntry>();

            long end = System.currentTimeMillis() + timeout;
            Looper.prepare();

            for (Object group_id : userEntry.getGroups()){
                GroupEntry.GetGroupEntry getter = new GroupEntry.GetGroupEntry((String) group_id, 5000){
                    public void onPostExecute(){
                        groupEntryGetters.remove(this);
                        groupResults.add(getResult());
                        if (!isSuccessful() || System.currentTimeMillis()>=end || groupEntryGetters.size()==0){
                            Looper.myLooper().quitSafely();
                        }
                    }
                };
                groupEntryGetters.add(getter);
                getter.start();
            }
            Looper.loop();

            if (groupEntryGetters.size()!=0) {
                Log.d(LOG_NAME, "Timeout");
                return;
            }

            result = new ArrayList<EventEntry>();
            for (GroupEntry groupEntry : groupResults){
                if (groupEntry==null) {
                    result = null;
                    return;
                }
                result.addAll(groupEntry.getGroupEvents(getCurrent,getPast));
            }
            result.addAll(userEntry.getUserEvents(getCurrent,getPast));
            return;
        }

        public ArrayList<EventEntry> getResult(){
            return result;
        }

        public boolean isSuccessful(){
            return result!=null;
        }

        public abstract void onPostExecute();
    }

    public abstract static class GetUserRelevantTodo extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserRelevantTodo";
        private static final int SLEEP_INCREMENT = 10;
        private ArrayList<TodoEntry> result = null;

        private UserEntry userEntry;
        private Integer timeout;
        private boolean getCurrent, getPast;
        private String userId;

        public GetUserRelevantTodo(UserEntry userEntry,Integer timeout,boolean getCurrent,boolean getPast,String userId){
            this.userEntry = userEntry;
            this.timeout = timeout;
            this.getCurrent = getCurrent;
            this.getPast = getPast;
            this.userId = userId;
        }

        @SuppressLint("LongLogTag")
        public void runMainBody() {
            ArrayList<GroupEntry> groupResults = new ArrayList<GroupEntry>();
            LinkedList<GroupEntry.GetGroupEntry> groupEntryGetters = new LinkedList<GroupEntry.GetGroupEntry>();

            long end = System.currentTimeMillis() + timeout;
            Looper.prepare();

            for (Object group_id : userEntry.getGroups()){
                GroupEntry.GetGroupEntry getter = new GroupEntry.GetGroupEntry((String) group_id, 5000){
                    public void onPostExecute(){
                        groupEntryGetters.remove(this);
                        groupResults.add(getResult());
                        if (!isSuccessful() || System.currentTimeMillis()>=end || groupEntryGetters.size()==0){
                            Looper.myLooper().quitSafely();
                        }
                    }
                };
                groupEntryGetters.add(getter);
                getter.start();
            }
            Looper.loop();

            if (groupEntryGetters.size()!=0) {
                Log.d(LOG_NAME, "Timeout");
                return;
            }

            result = new ArrayList<TodoEntry>();
            for (GroupEntry groupEntry : groupResults){
                if (groupEntry==null) {
                    result = null;
                    return;
                }
                for (TodoEntry te : groupEntry.getGroupTodo(getCurrent,getPast)){
                    if (te.getAssigned_to().equals(userId)) result.add(te);
                }
            }
            result.addAll(userEntry.getUserTodo(getCurrent,getPast));
            return;
        }

        public ArrayList<TodoEntry> getResult(){
            return result;
        }

        public boolean isSuccessful(){
            return result!=null;
        }

        public abstract void onPostExecute();
    }

    // Get UserEntry
    /*
    public static class GetUserEntry extends AsyncTask<Object,Boolean,UserEntry> {
        private static final String LOG_NAME = "UserEntry.GetUserEntry";
        private static final int SLEEP_INCREMENT = 10;

        // params: String userId, Integer timeout
        protected UserEntry doInBackground(Object... params) {

            if (params.length != 2) {
                Log.d(LOG_NAME, "Incorrect number of arguments");
                return null;
            }
            if (!(params[0] instanceof String)) {
                Log.d(LOG_NAME, "Incompatible 0th argument");
                return null;
            }
            if (!(params[1] instanceof Integer)) {
                Log.d(LOG_NAME, "Incompatible 1st argument");
                return null;
            }

            String userId = (String) params[0];
            Integer timeout = (Integer) params[1];

            UserEntry user[] = {null};

            final boolean[] fail = {false};
            final boolean[] finish = {false};

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            user[0] = document.toObject(UserEntry.class);
                            fail[0] = false;
                            finish[0] = true;

                        } else {
                            Log.d(LOG_NAME, "User ID not found");
                            fail[0] = true;
                            finish[0] = true;
                        }
                    } else {
                        Log.d(LOG_NAME, "Retrieval error");
                        fail[0] = true;
                        finish[0] = true;
                    }
                }
            });

            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (now < end) {
                try {
                    Thread.sleep(SLEEP_INCREMENT);
                } catch (InterruptedException e) {
                }
                now = System.currentTimeMillis();
                if (finish[0]) {
                    if (fail[0]) return null;
                    else break;
                }
            }
            if (!finish[0]) {
                Log.d(LOG_NAME, "Timeout");
                return null;
            }
            return user[0];
        }

        public void join() {
            while (getStatus() != Status.FINISHED) {
                try {
                    Thread.sleep(SLEEP_INCREMENT);
                } catch (InterruptedException e) {
                }
            }
        }
    }*/

    /*
    public static class GetUserRelevantEvent extends AsyncTask<Object,Boolean,UserEntry> {
        private static final String LOG_NAME = "UserEntry.GetUserRelevantEvent";
        private static final int SLEEP_INCREMENT = 10;

        // params: String userId, Integer timeout
        @SuppressLint("LongLogTag")
        protected UserEntry doInBackground(Object... params) {

            if (params.length != 4) {
                Log.d(LOG_NAME, "Incorrect number of arguments");
                return null;
            }
            if (!(params[0] instanceof UserEntry)) {
                Log.d(LOG_NAME, "Incompatible 0th argument");
                return null;
            }
            if (!(params[1] instanceof Integer)) {
                Log.d(LOG_NAME, "Incompatible 1st argument");
                return null;
            }
            if (!(params[2] instanceof Boolean) || !(params[3] instanceof Boolean)) {
                Log.d(LOG_NAME, "Incompatible 2nd/3rd argument");
                return null;
            }

            UserEntry user = (UserEntry) params[0];
            Integer timeout = (Integer) params[1];
            boolean getCurrent = (Boolean) params[2];
            boolean getPast = (Boolean) params[3];

            HashMap<String,GroupEntry.GetGroupEntry> groupEntryGetters = new HashMap<String,GroupEntry.GetGroupEntry>();

            for (Object grp:user.getGroups()){
                String groupId = (String) grp;
                GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry();
                getGroupEntry.execute(groupId,timeout);
                groupEntryGetters.put(groupid,getGroupEntry);
            }
        }
    }*/
}
