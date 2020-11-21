package com.example.beever.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupEntry {

    private Integer colour = 0;
    private String name = null, display_picture = null;
    private List<Object> member_list = null;
    private Map<String,Object> group_events = null, todo_list = null;
    private List<Object> chat;

    public GroupEntry(){}

    public GroupEntry(Integer colour, String name, List<Object> member_list, Map<String,Object> group_events, Map<String,Object> todo_list, List<Object> chat){
        setColour(colour);
        setName(name);
        setMember_list(member_list);
        setGroup_events(group_events);
        setTodo_list(todo_list);
        setChat(chat);
    }

    public void setColour(Integer colour){
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

    public Integer getColour(){
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

    public String toString(){
        return "Name: " + name + "\n"
                + "Member list: " + member_list.toString() + "\n"
                + "Colour: " + colour + "\n"
                + "Display picture: " + display_picture + "\n"
                + "Group events: " + group_events.toString() + "\n"
                + "Todo list: " + todo_list.toString() + "\n"
                + "Chat: " + chat.toString() + "\n";
    }
}
