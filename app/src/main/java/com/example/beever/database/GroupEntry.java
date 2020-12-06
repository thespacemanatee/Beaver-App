/*
Copyright (c) 2020 Beever

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.example.beever.database;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to represent group documents in the Firestore database, under the "groups" collection.
 * This class interfaces directly with Firestore's DocumentSnapshot.toObject(),
 * and so barely any additional fields beyond the essentials are added.
 *
 * Group documents follow this contract, the GroupEntry contract, in Firestore:
 * - <Group ID>: Document
 *    - name: String
 *    - display_picture: String (nullable)
 *    - colour: Long, strictly non-negative integer
 *    - member_list: List<Object>, minimum size 0
 *        - <User ID>...: String
 *    - group_events: Map<String,Object>, size 2
 *        - current: List<Object>, minimum size 0
 *            - <Event>...: Map<String,Object> represented by EventEntry
 *        - past: List<Object>, minimum size 0
 *            - <Event>...: Map<String,Object> represented by EventEntry
 *    - todo_list: Map<String,Object>, size 2
 *        - current: List<Object>, minimum size 0
 *            - <Todo>...: Map<String,Object> represented by TodoEntry
 *        - past: List<Object>, minimum size 0
 *            - <Todo>...: Map<String,Object> represented by TodoEntry
 *    - chat: List<Object>, minimum size 0
 *        - <Message>...: Map<String,Object> represented by ChatEntry
 *
 * To create an instance of this class manually, use only the 2nd constructor.
 **/

public class GroupEntry implements Parcelable, MapEntry {

    private String name = null, display_picture = null;
    private Long colour = (long) 0;
    private List<Object> member_list = null;
    private Map<String,Object> group_events = null, todo_list = null;
    private List<Object> chat = null;

    /**
     * Default constructor to be used by Firestore
     */
    public GroupEntry(){
        setMember_list(null);
        setGroup_events(null);
        setTodo_list(null);
        setChat(null);
    }

    /**
     * Alternative constructor to manually create groups
     * @param name: name of group
     * @param display_picture: URL for display picture, or null if not applicable
     * @param colour: #RRGGBB colour scheme for group in integer
     * @param member_list: list of members' user IDs
     * @param group_events: list of group events
     * @param todo_list: list of group todos
     * @param chat: list of chat messages for group
     */
    public GroupEntry(String name, String display_picture, Long colour, List<Object> member_list,
                      Map<String,Object> group_events, Map<String,Object> todo_list, List<Object> chat){
        setName(name);
        setDisplay_picture(display_picture);
        setColour(colour);
        setMember_list(member_list);
        setGroup_events(group_events);
        setTodo_list(todo_list);
        setChat(chat);
    }

    // Setters

    protected GroupEntry(Parcel in) {
        name = in.readString();
        display_picture = in.readString();
        if (in.readByte() == 0) {
            colour = null;
        } else {
            colour = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(display_picture);
        if (colour == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(colour);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupEntry> CREATOR = new Creator<GroupEntry>() {
        @Override
        public GroupEntry createFromParcel(Parcel in) {
            return new GroupEntry(in);
        }

        @Override
        public GroupEntry[] newArray(int size) {
            return new GroupEntry[size];
        }
    };

    public void setName(String name){
        this.name = name;
    }

    /**
     * Set display picture URL for group
     * @param display_picture URL for display picture, or null
     */
    public void setDisplay_picture(String display_picture){
        this.display_picture = display_picture;
    }

    /**
     * Set #RRGGBB colour. The function takes the intValue() of the argument provided, and if
     * this value is out of bounds, colour is set automatically to 0.
     * @param colour target #RRGGBB colour scheme for group
     */
    public void setColour(Long colour){
        Integer intColour = colour.intValue();
        this.colour = intColour>=0 && intColour<=16777215 ? (long) intColour : 0;
    }

    /**
     * Set member list. If a null pointer is provided a new list is created.
     * It is recommended to use addUserId() and removeUserId() instead.
     * @param member_list member list to use for updating
     */
    public void setMember_list(List<Object> member_list){
        this.member_list = member_list==null? new ArrayList<Object>() : member_list;
    }

    /**
     * Add user ID, if this user is not already in this group
     * @param userId user ID of user
     */
    public void addUserId(String userId){
        if (!member_list.contains(userId)) member_list.add(userId);
    }

    /**
     * Remove user ID, if this user is in this group
     * @param userId user ID of user
     */
    public void removeUserId(String userId){
        member_list.remove(userId);
    }

    /**
     * Set Map of group events. Note that if a map that does not follow the GroupEntry contract
     * is provided, a map with no event entries that satisfies said contract is created instead.
     * It is recommended to use modifyEventOrTodo() instead.
     * @param group_events Map of group events, or null
     */
    public void setGroup_events(Map<String,Object> group_events){
        if (group_events!=null && group_events.containsKey("current") && group_events.containsKey("past") &&
                group_events.get("current") instanceof List && group_events.get("past") instanceof List) {
            this.group_events = group_events;
            return;
        }
        this.group_events = new HashMap<String,Object>();
        this.group_events.put("current",new ArrayList<Object>());
        this.group_events.put("past",new ArrayList<Object>());
    }

    /**
     * Set Map of group todos. Note that if a map that does not follow the GroupEntry contract
     * is provided, a map with no event entries that satisfies said contract is created instead.
     * It is recommended to use modifyEventOrTodo() instead.
     * @param todo_list Map of group todos, or null
     */
    public void setTodo_list(Map<String,Object> todo_list){
        if (todo_list!=null && todo_list.containsKey("current") && todo_list.containsKey("past") &&
                todo_list.get("current") instanceof List && todo_list.get("past") instanceof List) {
            this.todo_list = todo_list;
            return;
        }
        this.todo_list = new HashMap<String,Object>();
        this.todo_list.put("current",new ArrayList<Object>());
        this.todo_list.put("past",new ArrayList<Object>());
    }

    /**
     * Add or remove an event or task from the group entry, for group event/todo
     * @param isEvent if true, add/remove event; otherwise add/remove todo
     * @param isCurrent if true, modify current event/todo list; otherwise modify past event/todo list
     * @param isAdd if true, add event/todo; else remove event/todo
     * @param eventOrTodo event/todo to modify
     */
    public void modifyEventOrTodo(boolean isEvent, boolean isCurrent, boolean isAdd, EventTodoEntry eventOrTodo){
        if (isEvent) assert eventOrTodo instanceof EventEntry;
        else assert eventOrTodo instanceof TodoEntry;
        Map<String,Object> selectCat = isEvent? group_events : todo_list;
        List<Object> selectList = isCurrent? (List<Object>) selectCat.get("current") : (List<Object>) selectCat.get("past");
        if (isAdd){
            selectList.add(eventOrTodo.retrieveRepresentation());
        }
        else selectList.remove(eventOrTodo.retrieveRepresentation());
    }

    /**
     * Set list of chat messages. Notes that if a null pointer is passed to the function the
     * function generates a new empty list instead.
     * It is recommended to use addChatEntry() instead.
     * @param chat list of chat messages, or null
     */
    public void setChat(List<Object> chat){
        this.chat = chat==null? new ArrayList<Object>() : chat;
    }

    /**
     * Add new ChatEntry to the group list of chat messages.
     * @param chatEntry chatEntry object to add (will be converted to suitable type)
     */
    public void addChatEntry(ChatEntry chatEntry){
        chat.add(chatEntry.retrieveRepresentation());
    }

    // Getters

    public String getName(){
        return name;
    }

    /**
     * Get group display picture URL, or null if no such URL exists
     * @return URL or null
     */
    public String getDisplay_picture(){
        return display_picture;
    }

    public Long getColour(){
        return colour;
    }

    /**
     * Get list of members' user IDs as strings
     * @return list of strings of user IDs
     */
    public List<Object> getMember_list(){
        return member_list;
    }

    /**
     * Get map of group events as a raw Map.
     * It is recommended to use getGroupEvents() instead.
     * @return Map of group events
     */
    public Map<String,Object> getGroup_events(){
        return group_events;
    }

    /**
     * Get a list of group events as EventEntry objects
     * @param getCurrent if true, get current events
     * @param getPast if true, get past events
     * @return list of events represented as EventEntry objects
     */
    public ArrayList<EventEntry> retrieveGroupEvents(boolean getCurrent, boolean getPast){
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

    /**
     * Get map of group todos as a raw Map.
     * It is recommended to use getGroupTodos() instead.
     * @return Map of group todos
     */
    public Map<String,Object> getTodo_list(){
        return todo_list;
    }

    /**
     * Get a list of group todos as TodoEntry objects
     * @param getCurrent if true, get current todos
     * @param getPast if true, get past todos
     * @return list of todos represented as TodoEntry objects
     */
    public ArrayList<TodoEntry> retrieveGroupTodos(boolean getCurrent, boolean getPast){
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

    /**
     * Get list of chat messages as a raw List.
     * It is recommended to use getGroupChat() instead.
     * @return list of chat messages
     */
    public List<Object> getChat(){
        return chat;
    }

    /**
     * Get list of group chat messages represented as ChatEntry objects.
     * @return list of ChatEntry objects
     */
    public ArrayList<ChatEntry> retrieveGroupChat(){
        ArrayList<ChatEntry> ret = new ArrayList<ChatEntry>();
        for (Object o:chat) {
            ret.add(new ChatEntry(o));
        }
        return ret;
    }

    // Miscellaneous functions

    /**
     * Check if this GroupEntry object equals another object
     * @param o object to check equality with
     * @return boolean for whether this object equals the other object
     */
    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof GroupEntry)) return false;
        GroupEntry other = (GroupEntry) o;
        return getName().equals(other.getName())
                && getColour().equals(other.getColour())
                && ((getDisplay_picture()==null && other.getDisplay_picture()==null)
                    || (getDisplay_picture()!=null && other.getDisplay_picture()!=null
                    && getDisplay_picture().equals(other.getDisplay_picture())))
                && getMember_list().equals(other.getMember_list())
                && getGroup_events().equals(other.getGroup_events())
                && getTodo_list().equals(other.getTodo_list())
                && getChat().equals(other.getChat());
    }

    /**
     * Return raw Map form of this entry
     * @return entry as nested Map object
     */
    public Map<String,Object> retrieveRepresentation(){
        HashMap<String,Object> ret = new HashMap<String,Object>();
        ret.put("name",getName());
        ret.put("display_picture",getDisplay_picture());
        ret.put("colour",getColour());
        ret.put("member_list",getMember_list());
        ret.put("group_events",getGroup_events());
        ret.put("todo_list",getTodo_list());
        ret.put("chat",getChat());
        return ret;
    }

    /**
     * Get string representation of this GroupEntry
     * @return string representation
     */
    public String toString(){
        return "GroupEntry({\n\tname=" + name + ",\n"
                + "\tdisplay_picture=" + display_picture + ",\n"
                + "\tcolour=" + colour + ",\n"
                + "\tmember_list=" + member_list.toString() + ",\n"
                + "\tgroup_events=" + group_events.toString() + ",\n"
                + "\ttodo_list=" + todo_list.toString() + ",\n"
                + "\tchat=" + chat.toString() + "\n})";
    }

    /**
     * Customized Thread subclass to get GroupEntry based on user ID.
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class GetGroupEntry extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.GetGroupEntry";
        private static final int SLEEP_INCREMENT = 10;
        private GroupEntry result = null;
        private boolean timeoutOccurred = false;
        private boolean exists = true;

        String groupId;
        Integer timeout;

        /**
         * Standard constructor
         * @param groupId ID of group for which we are getting entry
         * @param timeout timeout for retrieval of group entry
         */
        public GetGroupEntry(String groupId,Integer timeout){
            this.groupId = groupId;
            this.timeout = timeout;
        }

        // params: String userId, Integer timeout
        @SuppressLint("LongLogTag")
        /**
         * Main thread operation to execute
         */
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
                            exists = false;
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
                timeoutOccurred = true;
                return;
            }
            result = group[0];
            return;
        }

        public String getGroupId() {
            return groupId;
        }

        /**
         * Get query result as a GroupEntry
         * @return requested GroupEntry, if query succeeds, or null otherwise
         */
        public GroupEntry getResult(){
            return result;
        }

        /**
         * Check whether query is successful
         * @return boolean for whether query is successful
         */
        public boolean isSuccessful(){
            return result!=null;
        }

        /**
         * Abstract method for post-retrieval operations
         */
        public abstract void onPostExecute();

        /**
         * Check if failure occurred due to timeout (use only for failed queries)
         * @return true if timeout occurred
         */
        public boolean isTimedOut() {return timeoutOccurred;}

        /**
         * Check if failure occurred due to entry not existing (use only for failed queries)
         * @return true if entry does not exist
         */
        public boolean isEntryMissing() {return !exists;}
    }

    /**
     * Listener to facilitate operations every time a specific GroupEntry is updated.
     * To use, create with the constructor, define the operations to perform when
     * the relevant group document is first retrieved and then updated, and start the listener.
     */
    public abstract static class GroupEntryListener{
        private static final String LOG_NAME = "GroupEntry.GroupEntryListener";

        /**
         * Enum to indicate type of change in the group document
         */
        public enum StateChange {
            NO_CHANGE,
            NAME,
            COLOUR,
            CHAT,
            DISPLAY_PICTURE,
            MEMBER_LIST,
            GROUP_EVENTS,
            TODO_LIST
        };

        private String groupId;
        private GroupEntry groupEntry = null;
        private StateChange stateChange = StateChange.NO_CHANGE;
        private GetGroupEntry getGroupEntry = null;
        private boolean timeoutOccurred = false;
        private boolean exists = true;

        /**
         * Standard constructor for the GroupEntry Listener
         * @param groupId group ID whose document we are tracking
         * @param timeout timeout for initially getting the group entry in milliseconds
         */
        public GroupEntryListener(String groupId, Integer timeout){
            this.groupId = groupId;
            getGroupEntry = new GroupEntry.GetGroupEntry(groupId,timeout){
                public void onPostExecute(){
                    if (!isSuccessful()){
                        if (isTimedOut()) GroupEntryListener.this.timeoutOccurred = true;
                        else if (isEntryMissing()) GroupEntryListener.this.exists = false;
                        onSetupFailure();
                        return;
                    }
                    GroupEntryListener.this.groupEntry = getResult();
                    onPreListening();
                    startListening();
                }
            };
        }

        @SuppressLint("LongLogTag")
        /**
         * Start listener once initial attempt to get group entry has been made
         */
        public void startListening() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("groups").document(groupId);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(LOG_NAME,"Retrieval error");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        GroupEntry newGroupEntry = snapshot.toObject(GroupEntry.class);
                        if (groupEntry==null){
                            groupEntry = newGroupEntry;
                            return;
                        }
                        if (newGroupEntry.equals(groupEntry)){
                            stateChange = StateChange.NO_CHANGE;
                            return;
                        }
                        if (!newGroupEntry.getName().equals(groupEntry.getName())){
                            stateChange = StateChange.NAME;
                        }
                        else if (!newGroupEntry.getColour().equals(groupEntry.getColour())){
                            stateChange = StateChange.COLOUR;
                        }
                        else if (!newGroupEntry.getChat().equals(groupEntry.getChat())){
                            stateChange = StateChange.CHAT;
                        }
                        else if ((newGroupEntry.getDisplay_picture()==null && groupEntry.getDisplay_picture()!=null)
                                || (newGroupEntry.getDisplay_picture()!=null && groupEntry.getDisplay_picture()==null)
                                || (newGroupEntry.getDisplay_picture()!=null && groupEntry.getDisplay_picture()!=null
                                && !newGroupEntry.getDisplay_picture().equals(groupEntry.getDisplay_picture()))){
                            stateChange = StateChange.DISPLAY_PICTURE;
                        }
                        else if (!newGroupEntry.getMember_list().equals(groupEntry.getMember_list())){
                            stateChange = StateChange.MEMBER_LIST;
                        }
                        else if (!newGroupEntry.getGroup_events().equals(groupEntry.getGroup_events())){
                            stateChange = StateChange.GROUP_EVENTS;
                        }
                        else{
                            stateChange = StateChange.TODO_LIST;
                        }
                        groupEntry = newGroupEntry;
                        onListenerUpdate();
                    } else {
                        Log.d(LOG_NAME,"GroupEntry not found");
                        return;
                    }
                }
            });
        }

        /**
         * Start listener operation, beginning with initial GroupEntry retrieval
         */
        public void start(){
            getGroupEntry.start();
        }

        /**
         * Operations to execute after initial retrieval but before listening starts
         */
        public abstract void onPreListening();

        /**
         * Operations to execute on every GroupEntry update
         */
        public abstract void onListenerUpdate();

        /**
         * Check if a copy of the GroupEntry currently exists
         * @return boolean for whether GroupEntry exists
         */
        public boolean exists(){
            return groupEntry!=null;
        }

        /**
         * Get most updated GroupEntry copy, if it exists
         * @return GroupEntry
         */
        public GroupEntry getResult(){
            return groupEntry;
        }

        /**
         * Get nature of latest GroupEntry update
         * @return StateChange corresponding to update
         */
        public StateChange getStateChange(){
            return stateChange;
        }

        /**
         * Get group id of document that listener is listening for
         * @return group id
         */
        public String getGroupId(){
            return groupId;
        }

        /**
         * Handler function for if listener fails to set up
         */
        public abstract void onSetupFailure();

        /**
         * Check if failure occurred due to timeout (use only if setup failed)
         * @return true if timeout occurred
         */
        public boolean isTimedOut(){
            return timeoutOccurred;
        }

        /**
         * Check if failure occurred due to entry not existing (use only if setup failed)
         * @return true if entry does not exist
         */
        public boolean isEntryMissing(){
            return !exists;
        }

    }

    /**
     * Customized Thread subclass to update GetEntry.
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class SetGroupEntry extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.SetGroupEntry";
        private static final int SLEEP_INCREMENT = 10;
        private Boolean result = null;
        private boolean timeoutOccurred = false;

        private GroupEntry groupEntry;
        private String groupId;
        private Integer timeout;

        /**
         * Standard constructor.
         * @param groupEntry GroupEntry to update with
         * @param groupId group ID to update
         * @param timeout timeout for update in milliseconds
         */
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
            if (result==null) {
                Log.d(LOG_NAME, "Timeout");
                result = false;
                timeoutOccurred = true;
            }
            return;
        }

        /**
         * Check if update is successful
         * @return boolean for whether update is successful
         */
        public boolean isSuccessful(){
            return result!=null && result;
        }

        /**
         * Abstract method for what to do after query, in main thread
         */
        public abstract void onPostExecute();

        /**
         * Check whether failure is due to timeout (if failure occurred). Use only if query failed.
         * @return true if timeout occurred
         */
        public boolean isTimedOut(){
            return timeoutOccurred;
        }
    }

    /**
     * Customized Thread subclass to retrieve CURRENT events relevant to all of a group's members
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class GetGroupRelevantEvents extends AsyncGetter {
        private static final String LOG_NAME = "GroupEntry.GetGroupRelevantEvents";
        private ArrayList<EventEntry> result = null;
        private boolean timeoutOccurred = false;
        private boolean userExists = true;
        private boolean groupExists = true;

        GroupEntry groupEntry;
        Integer timeout;

        /**
         * Standard constructor
         * @param groupEntry the entry of the group whose members will be checked
         * @param timeout timeout for retrieving all data from database
         */
        public GetGroupRelevantEvents(GroupEntry groupEntry,Integer timeout){
            this.groupEntry = groupEntry;
            this.timeout = timeout;
        }

        @SuppressLint("LongLogTag")
        /**
         * Main thread operation to execute
         */
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
                            if (isTimedOut()) GetGroupRelevantEvents.this.timeoutOccurred = true;
                            else if (isEntryMissing()) GetGroupRelevantEvents.this.userExists = false;
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
                                            if (!isSuccessful()){
                                                if (isTimedOut()) GetGroupRelevantEvents.this.timeoutOccurred = true;
                                                else if (isEntryMissing()) GetGroupRelevantEvents.this.groupExists = false;
                                            }
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
                timeoutOccurred = true;
                return;
            }

            result = new ArrayList<EventEntry>();

            for (UserEntry u:finalUsers){
                if (u == null){
                    result = null;
                    return;
                }
                result.addAll(u.retrieveUserEvents(true,false));
            }

            for (GroupEntry g:finalGroups){
                if (g == null){
                    result = null;
                    return;
                }
                result.addAll(g.retrieveGroupEvents(true,false));
            }

            return;
        }

        /**
         * Get list of events relevant to group's members
         * @return list of events, if successful, or null otherwise
         */
        public ArrayList<EventEntry> getResult(){
            return result;
        }

        /**
         * Check if query was successful
         * @return boolean for whether query was successful
         */
        public boolean isSuccessful(){
            return result!=null;
        }

        /**
         * Abstract method for post-retrieval operations on main thread
         */
        public abstract void onPostExecute();

        /**
         * Check if failure occurred due to timeout (use only for failed queries)
         * @return true if timeout occurred
         */
        public boolean isTimedOut() {return timeoutOccurred;}

        /**
         * Check if failure occurred due to any user entry not existing (use only for failed queries)
         * @return true if any user entry does not exist
         */
        public boolean isUserEntryMissing() {return !userExists;}

        /**
         * Check if failure occurred due to any group entry not existing (use only for failed queries)
         * @return true if any group entry does not exist
         */
        public boolean isGroupEntryMissing() {return !groupExists;}

    }

    /**
     * Subclass of AsyncGetter used to update single fields in group entries safely. To make
     * entirely new group entries use SetGroupEntry instead.
     * To use, create an instance of this class and start it.
     */
    public abstract static class UpdateGroupEntry extends AsyncGetter{

        private static final String LOG_NAME = "GroupEntry.UpdateGroupEntry";
        private static final int SLEEP_INCREMENT = 10;

        /**
         * Enum to indicate what field to change, and how to change the field (for some fields)
         */
        public enum FieldChange{
            NAME,
            DISPLAY_PICTURE,
            COLOUR,
            MEMBER_LIST_ADD,
            MEMBER_LIST_REMOVE,
            GROUP_EVENTS_CURRENT_ADD,
            GROUP_EVENTS_CURRENT_REMOVE,
            GROUP_EVENTS_PAST_ADD,
            GROUP_EVENTS_PAST_REMOVE,
            TODO_LIST_CURRENT_ADD,
            TODO_LIST_CURRENT_REMOVE,
            TODO_LIST_PAST_ADD,
            TODO_LIST_PAST_REMOVE,
            CHAT_ADD,
            CHAT_REMOVE
        }

        private FieldChange fieldChange = null;
        private String groupId = null;
        private Object updateObject = null;
        private Integer timeout = null;
        private Boolean result = null;
        private boolean timeoutOccurred = false;

        /**
         * Standard constructor
         * @param groupId group id of entry to update
         * @param fieldChange field to change
         * @param updateObject value to use for update
         * @param timeout timeout in milliseconds
         */
        public UpdateGroupEntry(String groupId, FieldChange fieldChange, Object updateObject, int timeout){
            this.groupId = groupId;
            this.fieldChange = fieldChange;
            this.updateObject = updateObject;
            this.timeout = timeout;
        }

        /**
         * Main body of thread
         */
        @SuppressLint("LongLogTag")
        public void runMainBody(){
            String fieldString = null;
            boolean isList = false;
            boolean isAdd = true;
            switch (fieldChange){
                case NAME:
                    fieldString = "name";
                    assert updateObject instanceof String;
                    break;
                case DISPLAY_PICTURE:
                    fieldString = "display_picture";
                    assert updateObject instanceof String;
                    break;
                case COLOUR:
                    fieldString = "colour";
                    assert updateObject instanceof Long;
                    break;
                case MEMBER_LIST_ADD:
                    fieldString = "member_list";
                    isList = true;
                    assert updateObject instanceof String;
                    break;
                case MEMBER_LIST_REMOVE:
                    fieldString = "member_list";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof String;
                    break;
                case GROUP_EVENTS_CURRENT_ADD:
                    fieldString = "group_events.current";
                    isList = true;
                    assert updateObject instanceof Map || updateObject instanceof EventEntry;
                    break;
                case GROUP_EVENTS_CURRENT_REMOVE:
                    fieldString = "group_events.current";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof Map || updateObject instanceof EventEntry;
                    break;
                case GROUP_EVENTS_PAST_ADD:
                    fieldString = "group_events.past";
                    isList = true;
                    assert updateObject instanceof Map || updateObject instanceof EventEntry;
                    break;
                case GROUP_EVENTS_PAST_REMOVE:
                    fieldString = "group_events.past";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof Map || updateObject instanceof EventEntry;
                    break;
                case TODO_LIST_CURRENT_ADD:
                    fieldString = "todo_list.current";
                    isList = true;
                    assert updateObject instanceof Map || updateObject instanceof TodoEntry;
                    break;
                case TODO_LIST_CURRENT_REMOVE:
                    fieldString = "todo_list.current";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof Map || updateObject instanceof TodoEntry;
                    break;
                case TODO_LIST_PAST_ADD:
                    fieldString = "todo_list.past";
                    isList = true;
                    assert updateObject instanceof Map || updateObject instanceof TodoEntry;
                    break;
                case TODO_LIST_PAST_REMOVE:
                    fieldString = "todo_list.past";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof Map || updateObject instanceof TodoEntry;
                    break;
                case CHAT_ADD:
                    fieldString = "chat";
                    isList = true;
                    assert updateObject instanceof Map || updateObject instanceof ChatEntry;
                    break;
                case CHAT_REMOVE:
                    fieldString = "chat";
                    isList = true;
                    isAdd = false;
                    assert updateObject instanceof Map || updateObject instanceof ChatEntry;
                    break;
                default:
                    assert false : "Invalid field choice.";
                    break;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("groups").document(groupId);

            OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>(){
                public void onSuccess (Void aVoid){
                    result = true;
                }
            };

            OnFailureListener onFailureListener = new OnFailureListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(@NonNull Exception e) {
                    result = false;
                    Log.d(LOG_NAME,"Update failed");
                }
            };

            if (!isList) docRef.update(fieldString,updateObject).addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(onFailureListener);
            else {
                if (isAdd) docRef.update(fieldString, FieldValue.arrayUnion(updateObject))
                        .addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                else docRef.update(fieldString, FieldValue.arrayRemove(updateObject))
                        .addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
            }

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
            if (result==null) {
                Log.d(LOG_NAME, "Timeout");
                result = false;
                timeoutOccurred = true;
            }

            return;

        }

        /**
         * Check whether update was successful.
         * @return true if update was successful
         */
        public boolean isSuccessful(){
            return result;
        }

        /**
         * Check whether update failure was due to timeout (if update did fail). Use only if update failed.
         * @return true if cause is timeout
         */
        public boolean isTimedOut() {
            return timeoutOccurred;
        }

    }

}
