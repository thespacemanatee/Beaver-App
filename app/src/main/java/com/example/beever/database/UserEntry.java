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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to represent user documents in the Firestore database, under the "users" collection.
 * This class interfaces directly with Firestore's DocumentSnapshot.toObject(),
 * and so barely any additional fields beyond the essentials are added.
 *
 * User documents follow this contract, the UserEntry contract, in Firestore:
 * - <User ID>: Document
 *    - username: String
 *    - name: String
 *    - email: String
 *    - display_picture: String (nullable)
 *    - groups: List<Object>, minimum size 0
 *        - <Group ID>...: String
 *    - dashboard_grps: List<Object>, size 6
 *        - <Group ID>...: String
 *    - user_events: Map<String,Object>, size 2
 *        - current: List<Object>, minimum size 0
 *            - <Event>...: Map<String,Object> represented by EventEntry
 *        - past: List<Object>, minimum size 0
 *            - <Event>...: Map<String,Object> represented by EventEntry
 *    - todo_list: Map<String,Object>, size 2
 *        - current: List<Object>, minimum size 0
 *            - <Todo>...: Map<String,Object> represented by TodoEntry
 *        - past: List<Object>, minimum size 0
 *            - <Todo>...: Map<String,Object> represented by TodoEntry
 *
 * To create an instance of this class manually, use only the 2nd constructor.
**/

public class UserEntry implements Parcelable {

    /**
     * Set default maximum number of groups to feature on the dashboard
     */
    private static final int DASHBOARD_GRPS = 6;

    private String username = null, name = null, email = null, display_picture = null;
    private List<Object> groups = null, dashboard_grps = null;
    private Map<String,Object> user_events = null, todo_list = null;

    /**
     * Default constructor to be used by Firestore
     */
    public UserEntry(){
        setGroups(null);
        setDashboard_grps(null);
        setUser_events(null);
        setTodo_list(null);
    }

    /**
     * Alternative constructor to manually generate user document representations
     * @param username username
     * @param name user name, does not need to be unique
     * @param email email address in standard format
     * @param display_picture (nullable) URL to user display picture
     * @param groups list of group IDs
     * @param dashboard_grps fixed-size list of group IDs
     * @param user_events map of arrays of event entries
     * @param todo_list map of arrays of tasks
     */
    public UserEntry(String username, String name, String email, String display_picture, List<Object> groups,
                     List<Object> dashboard_grps, Map<String,Object> user_events, Map<String,Object> todo_list){
        setUsername(username);
        setName(name);
        setEmail(email);
        setGroups(groups);
        setDashboard_grps(dashboard_grps);
        setUser_events(user_events);
        setTodo_list(todo_list);
        setDisplay_picture(display_picture);
    }

    // Setters

    protected UserEntry(Parcel in) {
        username = in.readString();
        name = in.readString();
        email = in.readString();
        display_picture = in.readString();
    }

    public static final Creator<UserEntry> CREATOR = new Creator<UserEntry>() {
        @Override
        public UserEntry createFromParcel(Parcel in) {
            return new UserEntry(in);
        }

        @Override
        public UserEntry[] newArray(int size) {
            return new UserEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(display_picture);
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setDisplay_picture(String display_picture){
        this.display_picture = display_picture;
    }

    /**
     * Set list of group IDs. If a null object is provided, an empty list is provided
     * to comply with the UserEntry contract.
     * It is recommended to use addGroupId() and removeGroupId() instead.
     * @param groups list of group IDs
     */
    public void setGroups(List<Object> groups){
        this.groups = groups==null? new ArrayList<Object>() : groups;
    }

    /**
     * Add user to group (group ID to user's list) if user is not already in said group
     * @param groupId group ID
     */
    public void addGroupId(String groupId){
        if (!groups.contains(groupId)) groups.add(groupId);
    }

    /**
     * Remove user from group (group ID from user's list) if user is already in said group
     * @param groupId group ID
     */
    public void removeGroupId(String groupId){
        groups.remove(groupId);
    }

    /**
     * Set list of group IDs to feature on dashboard. If a null object or incorrectly-sized array
     * is provided, a DASHBOARD_GRPS-sized array is made instead to comply with UserEntry contract.
     * It is recommended to use assignDashboardGrp() instead.
     * @param dashboard_grps list of featured group IDs
     */
    public void setDashboard_grps(List<Object> dashboard_grps){
        if (dashboard_grps!=null && dashboard_grps.size()==DASHBOARD_GRPS){
            this.dashboard_grps = dashboard_grps;
            return;
        }
        this.dashboard_grps = new ArrayList<Object>();
        for (int i=0;i<DASHBOARD_GRPS;i++) this.dashboard_grps.add(null);
    }

    /**
     * Assign a group ID to be featured on dashboard
     * @param index index to put group on dashboard
     * @param group_id ID of group to feature
     */
    public void assignDashboardGrp(int index, String group_id){
        if (index<0 || index>=6) return;
        dashboard_grps.set(index, group_id);
    }

    /**
     * Set map for user events. If map does not follow UserEntry contract, one such map without
     * any event entries which follows said contract is created.
     * It is recommended to use modifyEventOrTodo() instead.
     * @param user_events map
     */
    public void setUser_events(Map<String,Object> user_events){
        if (user_events!=null && user_events.containsKey("current") && user_events.containsKey("past") &&
        user_events.get("current") instanceof List && user_events.get("past") instanceof List) {
            this.user_events = user_events;
            return;
        }
        this.user_events = new HashMap<String,Object>();
        this.user_events.put("current",new ArrayList<Object>());
        this.user_events.put("past",new ArrayList<Object>());
    }

    /**
     * Set map for personal tasks. If map does not follow UserEntry contract, one such map without
     * any todo entries which follows said contract is created.
     * It is recommended to use modifyEventOrTodo() instead.
     * @param todo_list map
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
     * Add or remove an event or task from the user entry, for personal event/todo
     * @param isEvent if true, add/remove event; otherwise add/remove todo
     * @param isCurrent if true, modify current event/todo list; otherwise modify past event/todo list
     * @param isAdd if true, add event/todo; else remove event/todo
     * @param eventOrTodo event/todo to modify
     */
    public void modifyEventOrTodo(boolean isEvent, boolean isCurrent, boolean isAdd, EventTodoEntry eventOrTodo){
        if (isEvent) assert eventOrTodo instanceof EventEntry;
        else assert eventOrTodo instanceof TodoEntry;
        Map<String,Object> selectCat = isEvent? user_events : todo_list;
        List<Object> selectList = isCurrent? (List<Object>) selectCat.get("current") : (List<Object>) selectCat.get("past");
        if (isAdd){
            selectList.add(eventOrTodo.retrieveRepresentation());
        }
        else selectList.remove(eventOrTodo.retrieveRepresentation());
    }

    // Getters

    public String getUsername(){
        return username;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    /**
     * Get display picture URL
     * @return display picture URL, or null if not applicable
     */
    public String getDisplay_picture() {
        return display_picture;
    }

    /**
     * Get list of group IDs for groups the user is in
     * @return list of group ID strings
     */
    public List<Object> getGroups(){
        return groups;
    }

    public List<Object> getDashboard_grps(){
        return dashboard_grps;
    }

    /**
     * Get user's personal events as a raw Map.
     * It is recommended to use getUserEvents() instead.
     * @return map for user events
     */
    public Map<String,Object> getUser_events(){return user_events;}

    /**
     * Get personal events corresponding to the user as EventEntry objects
     * @param getCurrent if true, get current events
     * @param getPast if true, get past events
     * @return list of events represented as EventEntry objects
     */
    public ArrayList<EventEntry> retrieveUserEvents(boolean getCurrent, boolean getPast){
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

    /**
     * Get user's personal todo list as a raw Map.
     * It is recommended to use getUserTodos() instead.
     * @return map for user todos
     */
    public Map<String,Object> getTodo_list(){return todo_list;}

    /**
     * Get personal todos corresponding to the user as TodoEntry objects
     * @param getCurrent if true, get current todos
     * @param getPast if true, get past todos
     * @return list of events represented as EventEntry objects
     */
    public ArrayList<TodoEntry> retrieveUserTodos(boolean getCurrent, boolean getPast){
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

    // Miscellaneous functions

    /**
     * Check if object equals this UserEntry
     * @param o object to check
     * @return boolean for whether object equals this UserEntry
     */
    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof UserEntry)) return false;
        UserEntry other = (UserEntry) o;
        return getName().equals(other.getName())
                && getUsername().equals(other.getUsername())
                && getEmail().equals(other.getEmail())
                && getGroups().equals(other.getGroups())
                && getDashboard_grps().equals(other.getDashboard_grps())
                && getUser_events().equals(other.getUser_events())
                && getTodo_list().equals(other.getTodo_list())
                && ((getDisplay_picture()==null && other.getDisplay_picture()==null)
                    || (getDisplay_picture()!=null && other.getDisplay_picture()!=null
                    && getDisplay_picture().equals(other.getDisplay_picture())));
    }

    /**
     * Get string representation of this UserEntry
     * @return string representation
     */
    public String toString(){
        return "UserEntry({\n\tusername=" + username + ",\n"
                + "\tname=" + name + ",\n"
                + "\temail=" + email + ",\n"
                + "\tdisplay_picture=" + display_picture + "\n"
                + "\tgroups=" + groups.toString() + ",\n"
                + "\tdashboard_grps=" + dashboard_grps.toString() + ",\n"
                + "\tuser_events=" + user_events.toString() + ",\n"
                + "\ttodo_list=" + todo_list.toString() + "\n})";
    }



    /**
     * Customized Thread subclass to get UserEntry based on user ID.
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class GetUserEntry extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserEntry";
        private static final int SLEEP_INCREMENT = 10;
        private UserEntry result = null;
        private boolean timeoutOccurred = false;
        private boolean exists = true;

        private String userId;
        private Integer timeout;

        /**
         * Constructor for thread object
         * @param userId user id to query for
         * @param timeout timeout for query in milliseconds
         */
        public GetUserEntry(String userId,Integer timeout){
            this.userId = userId;
            this.timeout = timeout;
        }

        /**
         * Main thread operation to execute
         */
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
                timeoutOccurred = true;
            }

            result = user[0];
            return;
        }

        /**
         * Get queried UserEntry
         * @return the queried UserEntry if it exists, or null otherwise
         */
        public UserEntry getResult(){
            return result;
        }

        /**
         * Check if query is successful
         * @return boolean value for whether query is successful
         */
        public boolean isSuccessful(){
            return result!=null;
        }

        /**
         * Abstract method for what to do after query, in main thread
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
     * Listener to facilitate operations every time a specific UserEntry is updated.
     * To use, create with the constructor, define the operations to perform when
     * the relevant user document is first retrieved and then updated, and start the listener.
     */
    public abstract static class UserEntryListener{
        private static final String LOG_NAME = "UserEntry.UserEntryListener";

        /**
         * Enum to indicate type of change in the user document
         */
        public enum StateChange {
            NO_CHANGE,
            NAME,
            USERNAME,
            EMAIL,
            DISPLAY_PICTURE,
            GROUPS,
            DASHBOARD_GRPS,
            USER_EVENTS,
            TODO_LIST
        };

        private String userId;
        private UserEntry userEntry = null;
        private UserEntry.UserEntryListener.StateChange stateChange = StateChange.NO_CHANGE;
        private UserEntry.GetUserEntry getUserEntry = null;
        private boolean timeoutOccurred = false;
        private boolean exists = true;

        /**
         * Standard constructor for the UserEntry Listener
         * @param userId user ID whose document we are tracking
         * @param timeout timeout for initially getting the user entry in milliseconds
         */
        public UserEntryListener(String userId, Integer timeout){
            this.userId = userId;
            getUserEntry = new UserEntry.GetUserEntry(userId,timeout){
                public void onPostExecute(){
                    if (!isSuccessful()){
                        if (isTimedOut()) UserEntryListener.this.timeoutOccurred = true;
                        else if (isEntryMissing()) UserEntryListener.this.exists = false;
                        onSetupFailure();
                        return;
                    }
                    UserEntryListener.this.userEntry = getResult();
                    onPreListening();
                    startListening();
                }
            };

        }

        @SuppressLint("LongLogTag")
        /**
         * Start listener once initial attempt to get user entry has been made
         */
        public void startListening() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(LOG_NAME,"Retrieval error");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        UserEntry newUserEntry = snapshot.toObject(UserEntry.class);
                        if (userEntry==null){
                            userEntry = newUserEntry;
                            return;
                        }
                        if (newUserEntry.equals(userEntry)){
                            stateChange = StateChange.NO_CHANGE;
                            return;
                        }
                        if (!newUserEntry.getName().equals(userEntry.getName())){
                            stateChange = StateChange.NAME;
                        }
                        else if (!newUserEntry.getUsername().equals(userEntry.getUsername())){
                            stateChange = StateChange.USERNAME;
                        }
                        else if (!newUserEntry.getEmail().equals(userEntry.getEmail())){
                            stateChange = StateChange.EMAIL;
                        }
                        else if ((newUserEntry.getDisplay_picture()==null && userEntry.getDisplay_picture()!=null)
                                || (newUserEntry.getDisplay_picture()!=null && userEntry.getDisplay_picture()==null)
                                || (newUserEntry.getDisplay_picture()!=null && userEntry.getDisplay_picture()!=null
                                && !newUserEntry.getDisplay_picture().equals(userEntry.getDisplay_picture()))){
                            stateChange = StateChange.DISPLAY_PICTURE;
                        }
                        else if (!newUserEntry.getGroups().equals(userEntry.getGroups())){
                            stateChange = StateChange.GROUPS;
                        }
                        else if (!newUserEntry.getDashboard_grps().equals(userEntry.getDashboard_grps())){
                            stateChange = StateChange.DASHBOARD_GRPS;
                        }
                        else if (!newUserEntry.getUser_events().equals(userEntry.getUser_events())){
                            stateChange = StateChange.USER_EVENTS;
                        }
                        else{
                            stateChange = StateChange.TODO_LIST;
                        }
                        userEntry = newUserEntry;
                        onListenerUpdate();
                    } else {
                        Log.d(LOG_NAME,"UserEntry not found");
                        return;
                    }
                }
            });
        }

        /**
         * Start listener operation, beginning with initial UserEntry retrieval
         */
        public void start(){
            getUserEntry.start();
        }

        /**
         * Operations to execute after initial retrieval but before listening starts
         */
        public abstract void onPreListening();

        /**
         * Operations to execute on every UserEntry update
         */
        public abstract void onListenerUpdate();

        /**
         * Check if a copy of the UserEntry currently exists
         * @return boolean for whether UserEntry exists
         */
        public boolean exists(){
            return userEntry!=null;
        }

        /**
         * Get most updated UserEntry copy, if it exists
         * @return UserEntry
         */
        public UserEntry getResult(){
            return userEntry;
        }

        /**
         * Get nature of latest UserEntry update
         * @return StateChange corresponding to update
         */
        public StateChange getStateChange(){
            return stateChange;
        }

        /**
         * Get user id of document that listener is listening for
         * @return user id
         */
        public String getUserId(){
            return userId;
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
     * Customized Thread subclass to update UserEntry.
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class SetUserEntry extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.SetUserEntry";
        private static final int SLEEP_INCREMENT = 10;
        private Boolean result = null;

        private UserEntry userEntry;
        private String userId;
        private Integer timeout;

        /**
         * Standard constructor.
         * @param userEntry UserEntry to update with
         * @param userId user ID to update
         * @param timeout timeout for update in milliseconds
         */
        public SetUserEntry(UserEntry userEntry, String userId, Integer timeout){
            this.userEntry = userEntry;
            this.userId = userId;
            this.timeout = timeout;
        }

        /**
         * Main thread execution body
         */
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
            if (result==null) {
                Log.d(LOG_NAME, "Timeout");
                result = false;
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
    }

    /**
     * Customized Thread subclass to retrieve events relevant to a user
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class GetUserRelevantEvents extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserRelevantEvents";
        private static final int SLEEP_INCREMENT = 10;
        private ArrayList<EventEntry> result = null;
        private boolean timeoutOccurred = false;
        private boolean groupExists = true;

        private UserEntry userEntry;
        private Integer timeout;
        private boolean getCurrent, getPast;

        /**
         * Standard constructor for GetUserRelevantEvents.
         * @param userEntry user entry to get events for
         * @param timeout timeout for getting all events
         * @param getCurrent if true, get current events
         * @param getPast if true, get past events
         */
        public GetUserRelevantEvents(UserEntry userEntry,Integer timeout,boolean getCurrent,boolean getPast){
            this.userEntry = userEntry;
            this.timeout = timeout;
            this.getCurrent = getCurrent;
            this.getPast = getPast;
        }

        @SuppressLint("LongLogTag")
        /**
         * Main thread operation to execute
         */
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
                            if (!isSuccessful()){
                                if (isTimedOut()) GetUserRelevantEvents.this.timeoutOccurred = true;
                                else if (isEntryMissing()) GetUserRelevantEvents.this.groupExists = false;
                            }
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
                timeoutOccurred = true;
                return;
            }

            result = new ArrayList<EventEntry>();
            for (GroupEntry groupEntry : groupResults){
                if (groupEntry==null) {
                    result = null;
                    return;
                }
                result.addAll(groupEntry.retrieveGroupEvents(getCurrent,getPast));
            }
            result.addAll(userEntry.retrieveUserEvents(getCurrent,getPast));
            return;
        }

        /**
         * Get list of events relevant to user
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
         * Check if failure occurred due to any group entry not existing (use only for failed queries)
         * @return true if any group entry does not exist
         */
        public boolean isGroupEntryMissing() {return !groupExists;}
    }

    /**
     * Customized Thread subclass to retrieve todos relevant to a user
     * To use, create with the constructor, define what is to be done when thread completes,
     * and start the thread object.
     * Class extends AsyncGetter, please see the latter for format.
     */
    public abstract static class GetUserRelevantTodos extends AsyncGetter {
        private static final String LOG_NAME = "UserEntry.GetUserRelevantTodos";
        private static final int SLEEP_INCREMENT = 10;
        private ArrayList<TodoEntry> result = null;
        private boolean timeoutOccurred = false;
        private boolean groupExists = true;

        private UserEntry userEntry;
        private Integer timeout;
        private boolean getCurrent, getPast;
        private String userId;


        /**
         * Standard constructor for this object.
         * @param userEntry entry of user for whom we get relevant todos
         * @param timeout timeout for getting all todos
         * @param getCurrent if true, get current todos
         * @param getPast if true, get past todos
         * @param userId ID of the user to check for (for filtering)
         */
        public GetUserRelevantTodos(UserEntry userEntry,Integer timeout,boolean getCurrent,boolean getPast,String userId){
            this.userEntry = userEntry;
            this.timeout = timeout;
            this.getCurrent = getCurrent;
            this.getPast = getPast;
            this.userId = userId;
        }

        @SuppressLint("LongLogTag")
        /**
         * Main execution body of thread
         */
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
                            if (!isSuccessful()){
                                if (isTimedOut()) GetUserRelevantTodos.this.timeoutOccurred = true;
                                else if (isEntryMissing()) GetUserRelevantTodos.this.groupExists = false;
                            }
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
                timeoutOccurred = true;
                return;
            }

            result = new ArrayList<TodoEntry>();
            for (GroupEntry groupEntry : groupResults){
                if (groupEntry==null) {
                    result = null;
                    return;
                }
                for (TodoEntry te : groupEntry.retrieveGroupTodos(getCurrent,getPast)){
                    if (te.getAssigned_to().equals(userId)) result.add(te);
                }
            }
            result.addAll(userEntry.retrieveUserTodos(getCurrent,getPast));
            return;
        }

        /**
         * Get list of todos relevant to user
         * @return list of todos, if successful
         */
        public ArrayList<TodoEntry> getResult(){
            return result;
        }

        /**
         * Check if query is successful
         * @return boolean for whether query is successful
         */
        public boolean isSuccessful(){
            return result!=null;
        }

        /**
         * Abstract method for post-query execution
         */
        public abstract void onPostExecute();

        /**
         * Check if failure occurred due to timeout (use only for failed queries)
         * @return true if timeout occurred
         */
        public boolean isTimedOut() {return timeoutOccurred;}

        /**
         * Check if failure occurred due to any group entry not existing (use only for failed queries)
         * @return true if any group entry does not exist
         */
        public boolean isGroupEntryMissing() {return !groupExists;}
    }
}