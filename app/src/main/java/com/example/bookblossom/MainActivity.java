package com.example.bookblossom;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Logging tag for debugging
    private FirebaseAuth firebaseAuth;
    DatabaseReference reference;
    String email = null, displayName = null, imageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        // Initialize FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is logged in and internet is available
        if (isInternetAvailable()) {
            isUserLoggedIn();
        } else {
            startMainPage();
        }
    }

    /**
     * Check if the user is logged in and retrieve user details if logged in.
     */
    private void isUserLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, retrieve user details from the database
            String uid = currentUser.getUid();
            reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User data exists, retrieve details
                        email = dataSnapshot.child("email").getValue(String.class);
                        displayName = dataSnapshot.child("displayName").getValue(String.class);
                        imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                        // Log user details
                        Log.d("MainActivity", "User details retrieved: " + email + ", " + displayName);

                        // Log retrieved user data
                        Log.d(TAG, "Retrieved user email: " + email);
                        Log.d(TAG, "Retrieved user name: " + displayName);
                        Log.d(TAG, "Retrieved user image URL: " + imageUrl);

                        Intent homePageIntent = new Intent(MainActivity.this, HomePage.class);
                        homePageIntent.putExtra("userEmail", email);
                        homePageIntent.putExtra("userName", displayName);
                        homePageIntent.putExtra("userImage", imageUrl);
                        startActivity(homePageIntent);
                        finish(); // Close the MainActivity

                    } else {
                        // User data does not exist
                        Log.d("MainActivity", "User data does not exist.");
                        startMainPage();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                    Log.e("MainActivity", "Database error: " + databaseError.getMessage());
                    startMainPage();
                }
            });
        } else {
            // User is not logged in
            Log.d("MainActivity", "User is not logged in.");
            startMainPage();
        }
    }

    private void startMainPage() {
        Intent mainPageIntent = new Intent(MainActivity.this, MainPage.class);
        startActivity(mainPageIntent);
        finish();
    }

    /**
     * Check if there is an active internet connection.
     */
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
