package com.example.beever.database;

import java.util.ArrayList;
import java.util.HashMap;
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

    public String toString(){
        return "GroupEntry({name=" + name + ",\n"
                + "member_list=" + member_list.toString() + ",\n"
                + "colour=" + colour + ",\n"
                + "display_picture=" + display_picture + ",\n"
                + "group_events=" + group_events + ",\n"
                + "todo_list=" + todo_list.toString() + ",\n"
                + "chat=" + chat.toString() + "})";
    }
}
