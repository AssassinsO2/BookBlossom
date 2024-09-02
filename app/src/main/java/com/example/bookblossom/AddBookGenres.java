package com.example.bookblossom;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookblossom.models.Genre;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddBookGenres extends AppCompatActivity {

    private static final String TAG = "AddGenresActivity";
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid();
        } else {
            // Handle case where user is not signed in
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // List of genres
        List<String> genres = Arrays.asList(
                "Adventure", "Anthology", "Art & Photography", "Autobiography", "Biography",
                "Business", "Children's Books", "Classic Literature", "Cookbooks", "Crafts & Hobbies",
                "Drama", "Economics", "Education", "Essays", "Fantasy",
                "Fiction", "Graphic Novels", "Health & Wellness", "Historical Fiction", "Horror",
                "Humor", "Memoir", "Music", "Mystery", "Non-Fiction",
                "Philosophy", "Poetry", "Politics", "Reference", "Religion & Spirituality",
                "Romance", "Science", "Science Fiction", "Self-Help", "Sports",
                "Technology", "Thriller", "Travel", "True Crime", "Young Adult"
        );

        // Sort genres alphabetically
        Collections.sort(genres);

        // Upload genres to Firebase
        uploadGenresToFirebase(genres);
    }

    private void uploadGenresToFirebase(List<String> genres) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Genres");

        for (String genreName : genres) {
            long timestamp = System.currentTimeMillis();

            Genre genre = new Genre();
            genre.setId(""+timestamp);
            genre.setGenre(genreName);
            genre.setUid(currentUserUid); // Set the UID to the current user's UID
            genre.setTimestamp(timestamp);

            databaseReference.child(""+timestamp).setValue(genre, (databaseError, databaseReference1) -> {
                if (databaseError != null) {
                    Log.e(TAG, "Failed to add genre: " + genreName + ". Error: " + databaseError.getMessage());
                    Toast.makeText(AddBookGenres.this, "Failed to add genre: " + genreName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Genre added: " + genreName);
                }
            });

            // To ensure unique IDs, sleep for 1 millisecond
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Log.e(TAG, "Delay: " + e.getMessage());
            }
        }
    }
}
