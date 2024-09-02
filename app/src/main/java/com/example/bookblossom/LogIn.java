package com.example.bookblossom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LogIn extends AppCompatActivity {

    private static final String TAG = "LogInActivity";
    private FirebaseAuth mAuth;
    TextInputLayout emailInputLayout, passwordInputLayout;
    TextInputEditText emailEditText, passwordEditText;
    String displayName = "null", imageUrl = "null", email, password;// Default value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.logIn);
        Button forgotPasswordButton = findViewById(R.id.xyz);

        // Set up listeners
        forgotPasswordButton.setOnClickListener(v -> forgotPassword());
        loginButton.setOnClickListener(v -> handleLogin());
    }

    /**
     * Handles the login process by validating input and signing in the user.
     */
    private void handleLogin() {
        email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        // Validate inputs
        if (!validateInputs()) return;

        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            fetchDisplayNameAndProceed(uid, email);
                        }
                    } else {
                        handleLoginFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithEmail:failure", e);
                    Toast.makeText(LogIn.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Validates user input fields.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.setHelperText("Email is required");
            setError(emailEditText, R.drawable.outlined_error_text_view);
            isValid = false;
        } else {
            emailInputLayout.setHelperText(null);
            clearError(emailInputLayout);
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.setHelperText("Password is required");
            setError(passwordEditText, R.drawable.outlined_error_text_view);
            isValid = false;
        } else {
            passwordInputLayout.setHelperText(null);
            clearError(passwordInputLayout);
        }

        return isValid;
    }

    /**
     * Handles login failure by updating UI based on the type of error.
     */
    private void handleLoginFailure(Exception exception) {
        Log.w(TAG, "signInWithEmail:failure", exception);
        if (exception instanceof FirebaseAuthInvalidUserException) {
            emailInputLayout.setHelperText("Email not registered");
            setError(findViewById(R.id.email), R.drawable.outlined_error_text_view);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            passwordInputLayout.setHelperText("Wrong password");
            setError(findViewById(R.id.password), R.drawable.outlined_error_text_view);
        } else {
            Toast.makeText(LogIn.this, "Authentication failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches user details from the database and proceeds to the next activity.
     */
    private void fetchDisplayNameAndProceed(String uid, String email) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    displayName = snapshot.child("displayName").getValue(String.class);
                    imageUrl = snapshot.child("imageUrl").getValue(String.class);
                }
                Log.d(TAG, "signInWithEmail:success");
                Toast.makeText(LogIn.this, "Authentication success.", Toast.LENGTH_SHORT).show();

                // Proceed to the next activity
                Intent exploreIntent = new Intent(LogIn.this, HomePage.class);
                exploreIntent.putExtra("userEmail", email);
                exploreIntent.putExtra("userName", displayName);
                exploreIntent.putExtra("userImage", imageUrl);
                startActivity(exploreIntent);
                finish(); // Close the login activity
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(LogIn.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initiates password reset process for the provided email.
     */
    private void forgotPassword() {
        TextInputEditText emailEditText = findViewById(R.id.email);
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LogIn.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        handleForgotPasswordFailure(task.getException());
                    }
                });
    }

    /**
     * Handles failure during password reset process.
     */
    private void handleForgotPasswordFailure(Exception exception) {
        Log.e(TAG, "Failed to send password reset email", exception);
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(LogIn.this, "Invalid user email", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LogIn.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets an error drawable for a TextInputEditText.
     */
    private void setError(TextInputEditText editText, int drawableResId) {
        editText.setBackgroundResource(drawableResId);
    }

    /**
     * Clears the error for a TextInputLayout.
     */
    private void clearError(TextInputLayout layout) {
        layout.setError(null);
    }
}
