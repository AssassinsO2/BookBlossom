package com.example.bookblossom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookblossom.adapters.AuthorAdapter;
import com.example.bookblossom.models.Author;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AuthorsFragment extends Fragment {

    private static final String TAG = "AuthorsFragment";
    private AuthorAdapter adapter;
    private List<Author> authorList;

    private DatabaseReference databaseAuthors;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authors, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_authors);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        authorList = new ArrayList<>();
        adapter = new AuthorAdapter(authorList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseAuthors = database.getReference().child("Authors");

        // Retrieve authors from Firebase
        fetchAuthorsFromFirebase();

        // Handle item click in RecyclerView
        adapter.setOnItemClickListener(position -> {
            // Get the clicked author
            Author clickedAuthor = authorList.get(position);

            // Open AuthorDetailActivity and pass author details
            openAuthorDetailActivity(clickedAuthor);
        });

        return view;
    }

    private void fetchAuthorsFromFirebase() {
        databaseAuthors.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                authorList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Author author = snapshot.getValue(Author.class);
                    if (author != null) {
                        authorList.add(author);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch authors: " + databaseError.getMessage());
            }
        });
    }

    private void openAuthorDetailActivity(Author author) {
        // Create intent to start AuthorDetailActivity
        Intent intent = new Intent(getActivity(), AuthorDetailActivity.class);
        intent.putExtra("authorName", author.getAuthorName());
        intent.putExtra("authorEmail", author.getAuthorEmail());
        intent.putExtra("aboutAuthor", author.getAboutAuthor());
        startActivity(intent);
    }
}
