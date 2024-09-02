package com.example.bookblossom;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AuthorDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_detail);

        // Retrieve author details from intent extras
        String authorName = getIntent().getStringExtra("authorName");
        String authorEmail = getIntent().getStringExtra("authorEmail");
        String aboutAuthor = getIntent().getStringExtra("aboutAuthor");

        // Display author details in TextViews or any other views as needed
        TextView textViewAuthorName = findViewById(R.id.name);
        TextView textViewAuthorEmail = findViewById(R.id.email);
        TextView textViewAboutAuthor = findViewById(R.id.about_author);

        textViewAuthorName.setText(authorName);
        textViewAuthorEmail.setText(authorEmail);
        textViewAboutAuthor.setText(aboutAuthor);
    }
}
