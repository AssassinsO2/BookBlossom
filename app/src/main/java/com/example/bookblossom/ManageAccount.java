package com.example.bookblossom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ManageAccount extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "ManageAccount";
    private static final String IMAGE_MIME_TYPE = "image/*";

    // Views
    private TextInputEditText nameEditText;
    private TextView emailTextView;
    private TextInputEditText dobEditText;
    private ImageView profileImageView;

    // Firebase
    private DatabaseReference userRef;
    private StorageReference storageReference;

    // Activity Result Launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        profileImageView.setImageURI(imageUri);
                        uploadImage(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        // Initialize Firebase Auth and Storage
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        nameEditText = findViewById(R.id.name);
        emailTextView = findViewById(R.id.email);
        dobEditText = findViewById(R.id.date_of_birth);
        profileImageView = findViewById(R.id.profile_image);
        Button btnSaveChanges = findViewById(R.id.btn_save_changes);
        Button btnForgotPassword = findViewById(R.id.btn_forgot_password);

        // Load user data if logged in
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            fetchUserData();
        } else {
            handleError("No user logged in");
        }

        // Setup save changes button click listener
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        // Setup forgot password button click listener
        btnForgotPassword.setOnClickListener(v -> forgotPassword());

        // Setup profile image click listener to choose or capture new image
        profileImageView.setOnClickListener(v -> chooseImage());
    }

    private void fetchUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String dateOfBirth = snapshot.child("dateOfBirth").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Update UI with user data
                    updateUI(displayName, email, dateOfBirth, imageUrl);
                } else {
                    handleError("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError("Database error: " + error.getMessage());
            }
        });
    }

    private void updateUI(String displayName, String email, String dateOfBirth, String imageUrl) {
        // Update TextInputEditText and TextView fields with fetched data
        nameEditText.setText(displayName != null ? displayName : "");
        emailTextView.setText(email != null ? email : "");
        dobEditText.setText(dateOfBirth != null ? dateOfBirth : "");

        // Load profile image using Glide
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.account_circle_black)
                .error(R.drawable.account_circle_black)
                .into(profileImageView);
    }

    private void saveChanges() {
        String newName = Objects.requireNonNull(nameEditText.getText()).toString().trim();
        String newDob = Objects.requireNonNull(dobEditText.getText()).toString().trim();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Update user details in Firebase Realtime Database
            userRef.child("displayName").setValue(newName);
            userRef.child("dateOfBirth").setValue(newDob);

            Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            handleError("No user logged in");
        }
    }

    private void forgotPassword() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.sendPasswordResetEmail(Objects.requireNonNull(currentUser.getEmail()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent to " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            handleError("Failed to send password reset email: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        } else {
            handleError("No user logged in");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType(IMAGE_MIME_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Image"));
    }

    private void uploadImage(Uri imageUri) {
        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String imagePathAndName = "Users/" + userID + "_image";
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(imagePathAndName);
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the uploaded image URL
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Update user's profile image URL in Firebase Realtime Database
                            userRef.child("imageUrl").setValue(imageUrl);

                            // Update ImageView with the new image using Glide
                            Glide.with(this)
                                    .load(imageUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.account_circle_black)
                                    .error(R.drawable.account_circle_black)
                                    .into(profileImageView);

                            Toast.makeText(this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> handleError("Failed to upload profile image: " + e.getMessage()));
        }
    }

    private void handleError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
