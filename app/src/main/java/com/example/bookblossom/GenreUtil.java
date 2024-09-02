package com.example.bookblossom;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GenreUtil {
    private static final String TAG = "GenreUtil";
    private static ArrayList<String> genreTitleArrayList = new ArrayList<>();
    private static ArrayList<String> genreIdArrayList = new ArrayList<>();

    public static void fetchGenreNames() {
        Log.d(TAG, "Loading Genre: Loading PDF categories...");

        genreTitleArrayList = new ArrayList<>();
        genreIdArrayList = new ArrayList<>();

        DatabaseReference genresRef = FirebaseDatabase.getInstance().getReference("Genres");
        genresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                genreTitleArrayList.clear();
                genreIdArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String genreId = ""+dataSnapshot.child("id").getValue(); // Assuming genreId is the key (if structured this way)
                    String genreTitle = ""+dataSnapshot.child("genre").getValue();

                    genreTitleArrayList.add(genreTitle);
                    genreIdArrayList.add(genreId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching genres from Firebase: " + error.getMessage());
            }
        });
    }

    public static String getGenreName(String genreId) {
        for (int i = 0; i < genreIdArrayList.size(); i++) {
            String storedGenreId = genreIdArrayList.get(i);
            if (storedGenreId.equals(genreId)) {
                String genreTitle = genreTitleArrayList.get(i);
                Log.d(TAG, "Match found for genreId " + genreId + ": " + genreTitle);
                return genreTitle;
            }
        }
        Log.d(TAG, "No match found for genreId: " + genreId);
        return "Unknown Genre";
    }
}
