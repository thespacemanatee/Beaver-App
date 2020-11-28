package com.example.beever.admin;

public class UserHelperClass {

    private String name, email, userID;

    //No-arg constructor
    public UserHelperClass() {
    }

    //Constructor that takes in user inputs during registration
    public UserHelperClass(String name, String email, String userID) {
        this.name = name;
        this.email = email;
        this.userID = userID;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
