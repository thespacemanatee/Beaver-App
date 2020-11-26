package com.example.beever.database;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

public class GroupEntry {

    // Store contents of group document ONLY (use GroupEntryExtended for easier access)
    private Long colour = (long) 0;
    private String name = null, display_picture = null;
    private List<Object> member_list = null;
    private Map<String,Object> group_events = null, todo_list = null;
    private List<Object> chat;

    public GroupEntry(){}

    public GroupEntry(Long colour, String name, List<Object> member_list, Map<String,Object> group_events, Map<String,Object> todo_list, List<Object> chat){
        setColour(colour);
        setName(name);
        setMember_list(member_list);
        setGroup_events(group_events);
        setTodo_list(todo_list);
        setChat(chat);
    }

    public void setColour(Long colour){
        this.colour = colour;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDisplay_picture(String display_picture){
        this.display_picture = display_picture;
    }

    public void setMember_list(List<Object> member_list){
        this.member_list = member_list==null? new ArrayList<Object>() : member_list;
    }

    public void setGroup_events(Map<String,Object> group_events){
        this.group_events = group_events==null? new HashMap<String,Object>() : group_events;
    }

    public void setTodo_list(Map<String,Object> todo_list){
        this.todo_list = todo_list==null? new HashMap<String,Object>() : todo_list;
    }

    public void setChat(List<Object> chat){
        this.chat = chat==null? new ArrayList<Object>() : chat;
    }

    public String getName(){
        return name;
    }

    public String getDisplay_picture(){
        return display_picture;
    }

    public Long getColour(){
        return colour;
    }

    public List<Object> getMember_list(){
        return member_list;
    }

    public List<Object> getChat(){
        return chat;
    }

    public Map<String,Object> getGroup_events(){
        return group_events;
    }

    public Map<String,Object> getTodo_list(){
        return todo_list;
    }

    // Add user to group, if user is not already in said group
    public void addUserId(String userId){
        if (!member_list.contains(userId)) member_list.add(userId);
    }

    // Remove user from group, if user is in said group
    public void removeUserId(String userId){
        member_list.remove(userId);
    }

    // Add or remove a group event or todo from the current or past event/todo list
    // isEvent: if true, add/remove to/from events list, else add/remove to/from todo list
    // isCurrent: if true, add/remove to/from current list, else add/remove to/from todo list
    // isAdd: if true, add the event/todo, else remove
    // eventOrTodo: the EventEntry/TodoEntry to add/remove - type matching will be asserted
    public void modifyEventOrTodo(boolean isEvent, boolean isCurrent, boolean isAdd, EventTodoEntry eventOrTodo){
        if (isEvent) assert eventOrTodo instanceof EventEntry;
        else assert eventOrTodo instanceof TodoEntry;
        Map<String,Object> selectCat = isEvent? group_events : todo_list;
        List<Object> selectList = isCurrent? (List<Object>) selectCat.get("current") : (List<Object>) selectCat.get("past");
        if (isAdd){
            selectList.add(eventOrTodo.getRepresentation());
        }
        else selectList.remove(eventOrTodo.getRepresentation());
    }

    public void modifyChat(boolean isAdd, ChatEntry chatEntry){
        if (isAdd){
            chat.add(chatEntry.getRepresentation());
        }
        else chat.remove(chatEntry.getRepresentation());
    }

    // Get list of group events
    // getCurrent: if true, get current event
    // getPast: if true, get past event(so if you pass false to the last 2 elements, you would get
    // an empty ArrayList)
    public ArrayList<EventEntry> getGroupEvents(boolean getCurrent, boolean getPast){
        ArrayList<EventEntry> ret = new ArrayList<EventEntry>();
        if (getCurrent){
            List<Object> currentEvents = (List<Object>) group_events.get("current");
            for (Object o:currentEvents) {
                ret.add(new EventEntry(o));
            }
        }
        if (getPast){
            List<Object> pastEvents = (List<Object>) group_events.get("past");
            for (Object o:pastEvents) {
                ret.add(new EventEntry(o));
            }
        }
        return ret;
    }

    // Again same, but for todo
    public ArrayList<TodoEntry> getGroupTodo(boolean getCurrent, boolean getPast){
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

    // Same but chats
    public ArrayList<ChatEntry> getGroupChat(){
        ArrayList<ChatEntry> ret = new ArrayList<ChatEntry>();
        for (Object o:chat) {
            ret.add(new ChatEntry(o));
        }
        return ret;
    }

    public String toString(){
        return "GroupEntry({\n\tname=" + name + ",\n"
                + "\tmember_list=" + member_list.toString() + ",\n"
                + "\tcolour=" + colour + ",\n"
                + "\tdisplay_picture=" + display_picture + ",\n"
                + "\tgroup_events=" + group_events.toString() + ",\n"
                + "\ttodo_list=" + todo_list.toString() + ",\n"
                + "\tchat=" + chat.toString() + "\n})";
    }

    public abstract static class GetGroupEntry extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.GetGroupEntry";
        private static final int SLEEP_INCREMENT = 10;
        private GroupEntry result = null;

        String groupId;
        Integer timeout;

        public GetGroupEntry(String groupId,Integer timeout){
            this.groupId = groupId;
            this.timeout = timeout;
        }

        // params: String userId, Integer timeout
        @SuppressLint("LongLogTag")
        public void runMainBody() {

            GroupEntry group[] = {null};
            final boolean[] finish = {false};

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("groups").document(groupId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            group[0] = document.toObject(GroupEntry.class);
                            finish[0] = true;

                        } else {
                            Log.d(LOG_NAME, "Group ID not found");
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
                now = System.currentTimeMillis();
                if (finish[0]) {
                    break;
                }
            }
            if (!finish[0]) {
                Log.d(LOG_NAME, "Timeout");
                return;
            }
            result = group[0];
            return;
        }

        public GroupEntry getResult(){
            return result;
        }

        public boolean isSuccessful(){
            return result!=null;
        }

        public abstract void onPostExecute();
    }

    public abstract static class SetGroupEntry extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.SetGroupEntry";
        private static final int SLEEP_INCREMENT = 10;
        private Boolean result = null;

        private GroupEntry groupEntry;
        private String groupId;
        private Integer timeout;

        public SetGroupEntry(GroupEntry groupEntry, String groupId, Integer timeout){
            this.groupEntry = groupEntry;
            this.groupId = groupId;
            this.timeout = timeout;
        }

        @SuppressLint("LongLogTag")
        public void runMainBody(){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("groups").document(groupId)
                    .set(groupEntry)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            result = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
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

    public abstract static class GetGroupRelevantEvents extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.GetGroupRelevantEvents";
        private static final int SLEEP_INCREMENT = 10;
        private ArrayList<EventEntry> result = null;

        GroupEntry groupEntry;
        Integer timeout;

        public GetGroupRelevantEvents(GroupEntry groupEntry,Integer timeout){
            this.groupEntry = groupEntry;
            this.timeout = timeout;
        }

        @SuppressLint("LongLogTag")
        public void runMainBody() {
            HashMap<String,Boolean> exploredGroups = new HashMap<String,Boolean>();
            LinkedList<UserEntry.GetUserEntry> userEntryGetters = new LinkedList<UserEntry.GetUserEntry>();
            ArrayList<GroupEntry> finalGroups = new ArrayList<GroupEntry>();
            ArrayList<UserEntry> finalUsers = new ArrayList<UserEntry>();

            long end = System.currentTimeMillis() + timeout;
            Looper.prepare();

            for (Object user_id : groupEntry.getMember_list()){
                UserEntry.GetUserEntry getter = new UserEntry.GetUserEntry((String) user_id, 5000){
                    public void onPostExecute(){
                        //Log.d("currentUserIdOnPostExecute", (String) user_id);
                        if (!isSuccessful()){
                            Looper.myLooper().quitSafely();
                            return;
                        }
                        finalUsers.add(getResult());
                        LinkedList<GroupEntry.GetGroupEntry> childThreadList = new LinkedList<GroupEntry.GetGroupEntry>();
                        if (getResult().getGroups().size()==0){
                            userEntryGetters.remove(this);
                            //Log.d("currentUserIdZeroBranch", (String) user_id);
                            if (System.currentTimeMillis()>=end || userEntryGetters.size()==0){
                                Looper.myLooper().quitSafely();
                            }
                            return;
                        }
                        //Log.d("currentUserIdNonZeroBranch", (String) user_id);
                        UserEntry.GetUserEntry self = this;
                        for (Object rawGroupId : getResult().getGroups()){
                            String groupId = (String) rawGroupId;
                            if (!exploredGroups.containsKey(groupId)){
                                exploredGroups.put(groupId,false);
                                GroupEntry.GetGroupEntry groupEntryGetter = new GroupEntry.GetGroupEntry(groupId,timeout){
                                    public void onPostExecute(){
                                        if (!isSuccessful() || System.currentTimeMillis()>=end) {
                                            Looper.myLooper().quitSafely();
                                            return;
                                        }
                                        finalGroups.add(getResult());
                                        childThreadList.remove(this);
                                        if (childThreadList.size()==0) userEntryGetters.remove(self);
                                        //Log.d("childThreadCount", String.valueOf(childThreadList.size()));
                                        //Log.d("userEntryGettersCount", String.valueOf(userEntryGetters.size()));
                                        //Log.d("currentGroupId",groupId);
                                        //Log.d("currentUserId", (String) user_id);
                                        if (System.currentTimeMillis()>=end || userEntryGetters.size()==0) Looper.myLooper().quitSafely();
                                    }
                                };
                                childThreadList.add(groupEntryGetter);
                                groupEntryGetter.start();
                            }

                        }
                        if (childThreadList.size()==0) userEntryGetters.remove(self);
                        if (System.currentTimeMillis()>=end || userEntryGetters.size()==0){
                            Looper.myLooper().quitSafely();
                        }

                    }
                };
                userEntryGetters.add(getter);
                getter.start();
            }
            Looper.loop();

            if (userEntryGetters.size()!=0) {
                Log.d(LOG_NAME, "Timeout");
                return;
            }

            result = new ArrayList<EventEntry>();

            for (UserEntry u:finalUsers){
                if (u == null){
                    result = null;
                    return;
                }
                result.addAll(u.getUserEvents(true,false));
            }

            for (GroupEntry g:finalGroups){
                if (g == null){
                    result = null;
                    return;
                }
                result.addAll(g.getGroupEvents(true,false));
            }

            return;
        }

        @SuppressLint("LongLogTag")
        public void runPhase2(ArrayList<UserEntry> userResults){
            result = new ArrayList<EventEntry>();
            HashMap<String,Boolean> groupId = new HashMap<String,Boolean>();

            for (UserEntry userEntry : userResults) {
                if (userEntry == null) {
                    result = null;
                    return;
                }
                result.addAll(userEntry.getUserEvents(true, false));
                for (Object o : userEntry.getGroups()) {
                    String group_id = (String) o;
                    groupId.put(group_id, true);
                }
            }

            ArrayList<GroupEntry> groupResults = new ArrayList<GroupEntry>();
            LinkedList<GroupEntry.GetGroupEntry> groupEntryGetters = new LinkedList<GroupEntry.GetGroupEntry>();

            long end = System.currentTimeMillis() + timeout;

            for (String groupIdEntry : groupId.keySet()){
                GroupEntry.GetGroupEntry getter = new GroupEntry.GetGroupEntry(groupIdEntry, 5000){
                    public void onPostExecute(){
                        groupEntryGetters.remove(this);
                        groupResults.add(getResult());
                        if (System.currentTimeMillis()>=end || groupEntryGetters.size()==0){
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

            for (GroupEntry groupEntry : groupResults){
                if (groupEntry==null) {
                    result = null;
                    return;
                }
                result.addAll(groupEntry.getGroupEvents(true,false));
            }
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
}
