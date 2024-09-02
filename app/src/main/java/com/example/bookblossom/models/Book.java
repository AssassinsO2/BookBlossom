package com.example.bookblossom.models;

import androidx.annotation.NonNull;

public class Book {

    private String id;
    private String bookName;
    private String authorName;
    private String genreId;
    private String year;
    private String pages;
    private String chapters;
    private String description;
    private String uploadedBy;
    private String imageUrl; // Firebase Storage URL for book image
    private String pdfUrl; // Firebase Storage URL for book pdf
    private long timestamp;
    private boolean favourite;
    private boolean toRead;
    private boolean currentlyReading;
    private String uid; // User ID of the person who uploaded the book

    // Default constructor required for Firebase
    public Book() {
    }

    // Parameterized constructor
    public Book(String id, String bookName, String authorName, String genreId, String year, String pages, String chapters, String description, String uploadedBy, String imageUrl, String pdfUrl, long timestamp, boolean favourite, boolean toRead, boolean currentlyReading, String uid) {
        this.id = id;
        this.bookName = bookName;
        this.authorName = authorName;
        this.genreId = genreId;
        this.year = year;
        this.pages = pages;
        this.chapters = chapters;
        this.description = description;
        this.uploadedBy = uploadedBy;
        this.imageUrl = imageUrl;
        this.pdfUrl = pdfUrl;
        this.timestamp = timestamp;
        this.favourite = favourite;
        this.toRead = toRead;
        this.currentlyReading = currentlyReading;
        this.uid = uid;
    }

    // Getters and Setters for all fields
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getBookName() { return bookName; }
    public void setBookName(@NonNull String bookName) { this.bookName = bookName; }

    @NonNull
    public String getAuthorName() { return authorName; }
    public void setAuthorName(@NonNull String authorName) { this.authorName = authorName; }

    @NonNull
    public String getGenreId() { return genreId; }
    public void setGenreId(@NonNull String genreId) { this.genreId = genreId; }

    @NonNull
    public String getYear() { return year; }
    public void setYear(@NonNull String year) { this.year = year; }

    @NonNull
    public String getPages() { return pages; }
    public void setPages(@NonNull String pages) { this.pages = pages; }

    @NonNull
    public String getChapters() { return chapters; }
    public void setChapters(@NonNull String chapters) { this.chapters = chapters; }

    @NonNull
    public String getDescription() { return description; }
    public void setDescription(@NonNull String description) { this.description = description; }

    @NonNull
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(@NonNull String uploadedBy) { this.uploadedBy = uploadedBy; }

    @NonNull
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(@NonNull String imageUrl) { this.imageUrl = imageUrl; }

    @NonNull
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(@NonNull String pdfUrl) { this.pdfUrl = pdfUrl; }

    @NonNull
    public String getUid() { return uid; }
    public void setUid(@NonNull String uid) { this.uid = uid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isFavourite() { return favourite; }
    public void setFavourite(boolean favourite) { this.favourite = favourite; }

    public boolean isToRead() { return toRead; }
    public void setToRead(boolean toRead) { this.toRead = toRead; }

    public boolean isCurrentlyReading() { return currentlyReading; }
    public void setCurrentlyReading(boolean currentlyReading) { this.currentlyReading = currentlyReading; }
}
