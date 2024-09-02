package com.example.bookblossom.models;

public class Author {
    private String authorName;
    private String authorEmail;
    private String aboutAuthor;
    private String uid;  // Add this field

    public Author() {
        // Default constructor required for calls to DataSnapshot.getValue(Author.class)
    }

    public Author(String authorName, String authorEmail, String aboutAuthor, String uid) {
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.aboutAuthor = aboutAuthor;
        this.uid = uid;  // Initialize this field
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAboutAuthor() {
        return aboutAuthor;
    }

    public void setAboutAuthor(String aboutAuthor) {
        this.aboutAuthor = aboutAuthor;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
