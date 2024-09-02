package com.example.bookblossom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;


import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FirebaseAuth mAuth;
    private TextInputLayout nameInputLayout, emailInputLayout, dobInputLayout, passwordInputLayout;
    private TextInputEditText nameEditText, emailEditText, dobEditText, passwordEditText;
    private ImageButton profilePicture;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        profilePicture = findViewById(R.id.profile_image);
        profilePicture.setOnClickListener(v -> openFileChooser());

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        profilePicture.setImageURI(imageUri);
                    }
                }
        );

        // Initialize TextInputLayouts
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        dobInputLayout = findViewById(R.id.dobInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        dobEditText = findViewById(R.id.date_of_birth);
        passwordEditText = findViewById(R.id.password);

        // Attach DateTextWatcher to date of birth field
        dobEditText.addTextChangedListener(new DateTextWatcher(dobEditText));

        Button signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(v -> {
            String name = Objects.requireNonNull(nameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            String dateOfBirth = Objects.requireNonNull(dobEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            // Validate all fields
            boolean valid = validateFields(name, email, dateOfBirth, password);

            if (valid) {
                Toast.makeText(SignUp.this, "Please wait...", Toast.LENGTH_LONG).show();
                createUser(name, email, password, dateOfBirth);
            } else {
                Toast.makeText(SignUp.this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields(String name, String email, String dateOfBirth, String password) {
        boolean valid = true;

        // Validate name
        if (name.isEmpty()) {
            nameInputLayout.setHelperText("Name is required");
            setError(nameEditText, R.drawable.outlined_error_text_view);
            valid = false;
        } else {
            nameInputLayout.setHelperText(null);
            setError(nameEditText, R.drawable.outlined_text_view);
            clearError(nameInputLayout);
        }

        // Validate email format
        if (email.isEmpty() || !isValidEmail(email)) {
            emailInputLayout.setHelperText("Invalid email format");
            setError(emailEditText, R.drawable.outlined_error_text_view);
            valid = false;
        } else {
            emailInputLayout.setHelperText(null);
            setError(emailEditText, R.drawable.outlined_text_view);
            clearError(emailInputLayout);
        }

        // Validate date of birth format (DD/MM/YYYY)
        if (dateOfBirth.isEmpty() || !isValidDateOfBirth(dateOfBirth)) {
            dobInputLayout.setHelperText("Invalid date or format (DD/MM/YYYY)");
            setError(dobEditText, R.drawable.outlined_error_text_view);
            valid = false;
        } else {
            dobInputLayout.setHelperText(null);
            setError(dobEditText, R.drawable.outlined_text_view);
            clearError(dobInputLayout);
        }

        // Validate password format (at least 8 characters with special and number)
        if (!isValidPassword(password)) {
            passwordInputLayout.setHelperText("Password must be at least 8 characters with special and number");
            setError(passwordEditText, R.drawable.outlined_error_text_view);
            valid = false;
        } else {
            passwordInputLayout.setHelperText(null);
            setError(passwordEditText, R.drawable.outlined_text_view);
            clearError(passwordInputLayout);
        }

        return valid;
    }

    private void createUser(String name, String email, String password, String dob) {
        Log.d(TAG, "Uploading User details...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String imagePathAndName = "Users/" + uid + "_image";

                            if (imageUri != null) {
                                StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference(imagePathAndName);

                                imageStorageReference.putFile(imageUri)
                                        .addOnSuccessListener(taskSnapshot -> {
                                            Log.d(TAG, "Image uploaded to storage successfully");

                                            // Get the download URL for the image
                                            imageStorageReference.getDownloadUrl()
                                                    .addOnSuccessListener(uri -> {
                                                        String imageUrl = uri.toString();
                                                        Log.d(TAG, "Image URL: " + imageUrl);

                                                        // Save user data with the image URL
                                                        saveUserData(name, email, dob, imageUrl, uid);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to get download URL for image: " + e.getMessage());
                                                        Toast.makeText(SignUp.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to upload image: " + e.getMessage());
                                            Toast.makeText(SignUp.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // No image selected, save user data without profile picture
                                saveUserData(name, email, dob, null, uid);
                            }
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailInputLayout.setHelperText("Email already registered");
                            setError(emailEditText, R.drawable.outlined_error_text_view);
                            Toast.makeText(SignUp.this, "User already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUp.this, "Sign up failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserData(String name, String email, String dob, @Nullable String imageUrl, String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", uid);
        userInfo.put("displayName", name);
        userInfo.put("email", email);
        userInfo.put("dateOfBirth", dob);
        if (imageUrl != null) {
            userInfo.put("imageUrl", imageUrl);
        }

        ref.child(uid).setValue(userInfo)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Log.d(TAG, "User profile stored in database.");
                        Intent homePageIntent = new Intent(SignUp.this, HomePage.class);
                        homePageIntent.putExtra("userEmail", email);
                        homePageIntent.putExtra("userName", name);
                        homePageIntent.putExtra("userImage", imageUrl);
                        startActivity(homePageIntent);
                    } else {
                        Log.e(TAG, "Failed to store user profile: " + Objects.requireNonNull(dbTask.getException()).getMessage());
                        Toast.makeText(SignUp.this, "Failed to store user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Picture"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri);
        }
    }

    // Method to set TextInputLayout box stroke color
    private void setError(TextInputEditText editText, int drawableResId) {
        editText.setBackgroundResource(drawableResId);
    }

    // Method to clear error on TextInputLayout
    private void clearError(TextInputLayout layout) {
        layout.setError(null);
    }

    // Method to validate email format using regex
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    // Method to validate date of birth format (DD/MM/YYYY) using regex and logical checks
    private boolean isValidDateOfBirth(String dob) {
        // Validate length
        if (dob.length() != 10) return false;

        // Validate format and value
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dob);
            if (date != null && date.before(new Date())) {
                return true;
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }

    // TextWatcher for automatic date formatting
    private static class DateTextWatcher implements TextWatcher {

        private final TextInputEditText editText;

        public DateTextWatcher(TextInputEditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed here
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No action needed here
        }

        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();

            if (!input.isEmpty()) {
                StringBuilder formatted = new StringBuilder(input);

                // Remove any existing separators
                formatted = new StringBuilder(formatted.toString().replaceAll("[/\\-]", ""));

                // Insert separators based on the length
                if (formatted.length() > 2) {
                    formatted.insert(2, '/');
                }
                if (formatted.length() > 5) {
                    formatted.insert(5, '/');
                }

                // Prevent recursive calls
                if (!input.equals(formatted.toString())) {
                    editText.removeTextChangedListener(this);
                    editText.setText(formatted.toString());
                    editText.setSelection(formatted.length());
                    editText.addTextChangedListener(this);
                }
            }
        }
    }

    // Method to validate password format (at least 8 characters with special character and number)
    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$";
        return Pattern.compile(passwordRegex).matcher(password).matches();
    }
}
