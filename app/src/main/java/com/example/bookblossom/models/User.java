package com.example.bookblossom.models;

public class User {
    public String name;
    public String email;
    public String dateOfBirth;
    public String password;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String dateOfBirth, String password) {
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
    }
}

