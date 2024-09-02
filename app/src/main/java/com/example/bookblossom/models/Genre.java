package com.example.bookblossom.models;

import androidx.annotation.NonNull;

public class Genre {
    private String id;
    private String genre;
    private String uid;
    private long timestamp;

    // Default constructor required for Firebase
    public Genre() {
        // Default constructor
    }

    // Parameterized constructor
    public Genre(@NonNull String id, @NonNull String genre, @NonNull String uid, long timestamp) {
        this.id = id;
        this.genre = genre;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    // Getter and Setter for id
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    // Getter and Setter for genre
    @NonNull
    public String getGenre() {
        return genre;
    }

    public void setGenre(@NonNull String genre) {
        this.genre = genre;
    }

    // Getter and Setter for uid
    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    // Setter for timestamp
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
