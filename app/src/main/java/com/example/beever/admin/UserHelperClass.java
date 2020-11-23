package com.example.beever.admin;

public class UserHelperClass {

    private String name, email;

    //No-arg constructor
    public UserHelperClass() {
    }

    //Constructor that takes in user inputs during registration
    public UserHelperClass(String name, String email) {
        this.name = name;
        this.email = email;
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
}
