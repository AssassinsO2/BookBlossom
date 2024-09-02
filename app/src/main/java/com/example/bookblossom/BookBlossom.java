package com.example.bookblossom;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class BookBlossom extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static String formatTimeStamp(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        return DateFormat.format("dd/MM/yyyy", calendar).toString();
    }

    private static void performDatabaseOperation(Context context, String bookId, String listType, boolean isAddOperation) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> data = new HashMap<>();
        data.put("bookId", bookId);
        data.put("timestamp", timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference listRef = reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child(listType).child(bookId);

        if (isAddOperation) {
            listRef.setValue(data)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Book successfully added to " + listType, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to add book to " + listType + " due to " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            listRef.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Removed book from " + listType, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove book from " + listType + " due to " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public static void addToFavourites(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "Favourites", true);
    }

    public static void removeFromFavorites(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "Favourites", false);
    }

    public static void addToToRead(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "ToRead", true);
    }

    public static void removeFromToRead(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "ToRead", false);
    }

    public static void addToCurrentlyReading(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "CurrentlyReading", true);
    }

    public static void removeFromCurrentlyReading(Context context, String bookId) {
        performDatabaseOperation(context, bookId, "CurrentlyReading", false);
    }
}
