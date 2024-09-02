package com.example.bookblossom.models;

public class Review {

    String id;
    String bookId;
    String timestamp;
    String review;
    String uid;

    public Review() {

    }
    public Review(String id, String bookId, String timestamp, String review, String uid) {
        this.id = id;
        this.bookId = bookId;
        this.timestamp = timestamp;
        this.review = review;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
