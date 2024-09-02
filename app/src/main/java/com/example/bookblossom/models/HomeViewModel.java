package com.example.bookblossom.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Book>> books = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Genre>> genres = new MutableLiveData<>();
    private final DatabaseReference mBooksDatabase = FirebaseDatabase.getInstance().getReference("Books");
    private final DatabaseReference mGenresDatabase = FirebaseDatabase.getInstance().getReference("Genres");

    public HomeViewModel() {
        fetchBooksFromFirebase();
        fetchGenresFromFirebase();
    }

    // Provides the current list of books as LiveData
    public LiveData<List<Book>> getBooks() {
        return books;
    }

    // Provides the current list of genres as LiveData
    public LiveData<ArrayList<Genre>> getGenres() {
        return genres;
    }

    // Fetches books data from Firebase Realtime Database
    private void fetchBooksFromFirebase() {
        mBooksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Book> bookList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
                books.setValue(bookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log the error for debugging purposes
                Log.e("HomeViewModel", "Failed to fetch books from Firebase", databaseError.toException());
            }
        });
    }

    // Fetches genres data from Firebase Realtime Database
    private void fetchGenresFromFirebase() {
        mGenresDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Genre> genreList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Genre genre = snapshot.getValue(Genre.class);
                    if (genre != null) {
                        genreList.add(genre);
                    }
                }
                genres.setValue(genreList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log the error for debugging purposes
                Log.e("HomeViewModel", "Failed to fetch genres from Firebase", databaseError.toException());
            }
        });
    }
}
