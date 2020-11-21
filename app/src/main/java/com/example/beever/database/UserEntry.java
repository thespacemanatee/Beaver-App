package com.example.beever.database;

import java.util.ArrayList;
import java.util.List;

public class UserEntry {
    private String username = null, name = null, email = null;
    private List<Object> groups = new ArrayList<Object>(), dashboard_grps = new ArrayList<Object>();

    public UserEntry(){}

    public UserEntry(String username, String name, String email, List<Object> groups, List<Object> dashboard_grps){
        setUsername(username);
        setName(name);
        setEmail(email);
        setGroups(groups);
        setDashboard_grps(dashboard_grps);
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

    public void setGroups(List<Object> groups){
        this.groups = groups==null? new ArrayList<Object>() : groups;
    }

    public void setDashboard_grps(List<Object> dashboard_grps){
        this.dashboard_grps = dashboard_grps==null? new ArrayList<Object>() : dashboard_grps;
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

    public String toString(){
        return "Username: " + username + "\n"
                + "Name: " + name + "\n"
                + "Email: " + email + "\n"
                + "Groups: " + groups.toString() + "\n"
                + "Dashboard groups: " + dashboard_grps.toString() + "\n";
    }
}
