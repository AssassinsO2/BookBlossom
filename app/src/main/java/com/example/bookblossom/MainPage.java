package com.example.bookblossom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);

        // Initialize buttons
        Button signUp = findViewById(R.id.signUp);
        Button logIn = findViewById(R.id.logIn);

        signUp.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigating to SignUp activity.");
            Intent signUpPage = new Intent(MainPage.this, SignUp.class);
            startActivity(signUpPage);
        });

        logIn.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigating to LogIn activity.");
            Intent loginPage = new Intent(MainPage.this, LogIn.class);
            startActivity(loginPage);
        });
    }

}
