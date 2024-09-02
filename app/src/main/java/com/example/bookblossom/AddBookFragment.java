package com.example.bookblossom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookblossom.databinding.FragmentAddBookBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Objects;

public class AddBookFragment extends Fragment {

    String userEmail;
    private ImageView bookImageView;

    private ProgressDialog progressDialog;
    private Uri pdfUri = null;
    private Uri imageUri = null;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PDF_REQUEST = 2;
    private static final String TAG = "AddBookFragment";

    FirebaseAuth firebaseBookAuth, firebaseAuthorAuth;
    TextView selectedPdfTextView;
    DatabaseReference refBook, refAuthor;

    private ArrayList<String> genreTitleArrayList, genreIdArrayList;

    private FragmentAddBookBinding binding;
    private String selectedGenreId, selectGenreTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBookBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        assert getArguments() != null;
        userEmail = getArguments().getString("userEmail");

        firebaseBookAuth = FirebaseAuth.getInstance();
        firebaseAuthorAuth = FirebaseAuth.getInstance();
        loadGenres();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        TextInputLayout bookNameInputLayout = binding.bookNameInputLayout;
        TextInputLayout authorNameInputLayout = binding.authorNameInputLayout;
        TextInputLayout authorEmailInputLayout = binding.authorEmailInputLayout;
        TextView genre = binding.genreTv;
        TextInputLayout yearInputLayout = binding.yearInputLayout;
        TextInputLayout pagesInputLayout = binding.pagesInputLayout;
        TextInputLayout chaptersInputLayout = binding.chaptersInputLayout;
        TextInputLayout bookDescriptionInputLayout = binding.bookDescriptionInputLayout;
        TextInputLayout aboutAuthorInputLayout = binding.aboutAuthorInputLayout;

        TextInputEditText inputBookName = binding.bookName;
        TextInputEditText inputAuthorName = binding.authorName;
        TextInputEditText inputAuthorEmail = binding.authorEmail;
        TextInputEditText inputYear = binding.year;
        TextInputEditText inputPages = binding.pages;
        TextInputEditText inputChapters = binding.chapters;
        TextInputEditText inputBookDescription = binding.bookDescription;
        TextInputEditText inputAboutAuthor = binding.aboutAuthor;
        Button buttonSelectImage = binding.buttonSelectImage;
        Button buttonSelectPdf = binding.buttonSelectPdf;
        Button buttonAddBook = binding.buttonAddBook;
        bookImageView = binding.bookImageView;
        selectedPdfTextView = binding.selectedPdfTextView;

        binding.genreTv.setOnClickListener(v -> genrePickDialog());

        buttonSelectImage.setOnClickListener(v -> openGallery());
        buttonSelectPdf.setOnClickListener(v -> openPdfSelector());

        buttonAddBook.setOnClickListener(v -> {
            String bookName = Objects.requireNonNull(inputBookName.getText()).toString().trim();
            String authorName = Objects.requireNonNull(inputAuthorName.getText()).toString().trim();
            String authorEmail = Objects.requireNonNull(inputAuthorEmail.getText()).toString().trim();
            String genreStr = genre.getText().toString().trim();
            String year = Objects.requireNonNull(inputYear.getText()).toString().trim();
            String pages = Objects.requireNonNull(inputPages.getText()).toString().trim();
            String chapters = Objects.requireNonNull(inputChapters.getText()).toString().trim();
            String bookDescription = Objects.requireNonNull(inputBookDescription.getText()).toString().trim();
            String aboutAuthor = Objects.requireNonNull(inputAboutAuthor.getText()).toString().trim();

            if (bookName.isEmpty() || authorName.isEmpty() || authorEmail.isEmpty() || genreStr.isEmpty() || year.isEmpty() || pages.isEmpty() || chapters.isEmpty() || bookDescription.isEmpty() || aboutAuthor.isEmpty() || pdfUri == null || imageUri == null) {
                if (bookName.isEmpty()) {
                    bookNameInputLayout.setHelperText("Book Name is required");
                    setError(inputBookName, R.drawable.outlined_error_text_view);
                } else {
                    bookNameInputLayout.setHelperText(null);
                    setError(inputBookName, R.drawable.outlined_text_view);
                    clearError(bookNameInputLayout);
                }

                if (authorName.isEmpty()) {
                    authorNameInputLayout.setHelperText("Author Name is required");
                    setError(inputAuthorName, R.drawable.outlined_error_text_view);
                } else {
                    authorNameInputLayout.setHelperText(null);
                    setError(inputAuthorName, R.drawable.outlined_text_view);
                    clearError(authorNameInputLayout);
                }

                if (authorEmail.isEmpty()) {
                    authorEmailInputLayout.setHelperText("Author Email is required");
                    setError(inputAuthorEmail, R.drawable.outlined_error_text_view);
                } else {
                    authorEmailInputLayout.setHelperText(null);
                    setError(inputAuthorEmail, R.drawable.outlined_text_view);
                    clearError(authorEmailInputLayout);
                }

                if (year.isEmpty()) {
                    yearInputLayout.setHelperText("Year is required");
                    setError(inputYear, R.drawable.outlined_error_text_view);
                } else {
                    yearInputLayout.setHelperText(null);
                    setError(inputYear, R.drawable.outlined_text_view);
                    clearError(yearInputLayout);
                }

                if (pages.isEmpty()) {
                    pagesInputLayout.setHelperText("Pages is required");
                    setError(inputPages, R.drawable.outlined_error_text_view);
                } else {
                    pagesInputLayout.setHelperText(null);
                    setError(inputPages, R.drawable.outlined_text_view);
                    clearError(pagesInputLayout);
                }

                if (chapters.isEmpty()) {
                    chaptersInputLayout.setHelperText("Chapters is required");
                    setError(inputChapters, R.drawable.outlined_error_text_view);
                } else {
                    chaptersInputLayout.setHelperText(null);
                    setError(inputChapters, R.drawable.outlined_text_view);
                    clearError(chaptersInputLayout);
                }

                if (bookDescription.isEmpty()) {
                    bookDescriptionInputLayout.setHelperText("Book Description is required");
                } else {
                    bookDescriptionInputLayout.setHelperText(null);
                }

                if (aboutAuthor.isEmpty()) {
                    aboutAuthorInputLayout.setHelperText("About Author is required");
                } else {
                    aboutAuthorInputLayout.setHelperText(null);
                }

            } else {
                // Check if year is valid (less than or equal to current year)
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int y = Integer.parseInt(year);

                if (y > currentYear) {
                    yearInputLayout.setHelperText("Year should not exceed current year");
                    setError(inputYear, R.drawable.outlined_error_text_view);
                } else {
                    yearInputLayout.setHelperText(null);
                    setError(inputYear, R.drawable.outlined_text_view);
                    clearError(yearInputLayout);

                    // Proceed with further validation or upload process
                    checkBookExists(bookName, exists -> {
                        if (exists) {
                            Toast.makeText(getContext(), "Book already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            uploadFilesToStorage(bookName, authorName, authorEmail, selectedGenreId, year, pages, chapters, bookDescription, aboutAuthor);
                        }
                    });
                }
            }
        });

        return view;
    }

    private void loadGenres() {
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

    private void genrePickDialog() {
        String[] genreTitleArray = new String [genreTitleArrayList.size()];
        String[] genreIdArray = new String [genreIdArrayList.size()];

        for (int i = 0; i < genreTitleArrayList.size(); i++) {
            genreTitleArray[i] = genreTitleArrayList.get(i);
        }
        for (int i = 0; i < genreIdArrayList.size(); i++) {
            genreIdArray[i] = genreIdArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(genreTitleArray, (dialog, which) -> {
            selectGenreTitle = genreTitleArrayList.get(which);
            selectedGenreId = genreIdArrayList.get(which);

            binding.genreTv.setText(selectGenreTitle);

            Log.d(TAG, "onClick: Selected Genre: " + selectedGenreId + " " + selectGenreTitle);
        });

        // Set a custom adapter to modify the dialog item style
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.types_of_genres, genreTitleArray);
        builder.setAdapter(adapter, (dialog, which) -> {
            String genre = genreTitleArray[which];
            selectedGenreId = genreIdArray[which];
            binding.genreTv.setText(genre);
            Log.d(TAG, "onClick: Selected Genre: " + genre);
        });

        builder.show();
    }

    private void openPdfSelector() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Log.d(TAG, "Image successfully selected");

                imageUri = data.getData();
                bookImageView.setImageURI(imageUri);
                Log.d(TAG, "Image successfully selected: " + imageUri);
            }
        }
        if (requestCode == PICK_PDF_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Log.d(TAG, "PDF successfully selected");

                pdfUri = data.getData();
                selectedPdfTextView.setText("PDF: " + getFileName(pdfUri));
                Log.d(TAG, "PDF successfully selected: " + pdfUri);
            }
        }
    }

    private void uploadFilesToStorage(String bookName, String authorName, String authorEmail, String selectedGenreId, String year, String pages, String chapters, String bookDescription, String aboutAuthor) {
        Log.d(TAG, "Upload Files to storage: uploading to storage...");

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String pdfPathAndName = "Books/" + timestamp + "_pdf";
        String imagePathAndName = "Books/" + timestamp + "_image";

        StorageReference pdfStorageReference = FirebaseStorage.getInstance().getReference(pdfPathAndName);
        StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference(imagePathAndName);

        if (firebaseBookAuth.getCurrentUser() == null) {
            // Handle case where user is not signed in
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Please sign in to upload Files", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pdfUri == null) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Please select a Files to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        pdfStorageReference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                    Log.d(TAG, "onSuccess: getting PDF url");

                    Task<Uri> pdfUriTask  = taskSnapshot.getStorage().getDownloadUrl();
                    while (!pdfUriTask .isSuccessful());
                    Uri uploadedPdfUri = pdfUriTask .getResult();
                    if (uploadedPdfUri != null) {
                        String uploadedPdfUrl = uploadedPdfUri.toString();

                        Log.d(TAG, "PDF URL: " + uploadedPdfUrl);

                        imageStorageReference.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot1 -> {
                                    Log.d(TAG, "onSuccess: Image uploaded to storage...");
                                    Log.d(TAG, "onSuccess: getting Image url");

                                    Task<Uri> imageUriTask  = taskSnapshot1.getStorage().getDownloadUrl();
                                    while (!imageUriTask .isSuccessful());
                                    Uri uploadedImageUri = imageUriTask .getResult();
                                    if (uploadedImageUri != null) {
                                        String uploadedImageUrl = uploadedImageUri.toString();

                                        Log.d(TAG, "Image URL: " + uploadedImageUrl);

                                        uploadFilesInfoToDb(uploadedPdfUrl, uploadedImageUrl, timestamp, bookName, authorName, authorEmail, selectedGenreId, year, pages, chapters, bookDescription, aboutAuthor);
                                    } else {
                                        progressDialog.dismiss();
                                        Log.e(TAG, "onFailure: Failed to get Image URL");
                                        Toast.makeText(getContext(), "Failed to get Image URL", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Log.e(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                                    Toast.makeText(getContext(), "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "onFailure: Failed to get Image URL");
                        Toast.makeText(getContext(), "Failed to get Image URL", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                    Toast.makeText(getContext(), "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }

    private void uploadFilesInfoToDb(String uploadPdfUrl, String uploadImageUrl, long timestamp, String bookName, String authorName, String authorEmail, String selectedGenreId, String year, String pages, String chapters, String bookDescription, String aboutAuthor) {
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf and Image info to firebase db...");
        Log.e(TAG, "Selected GenreId: " + selectedGenreId);

        progressDialog.setMessage("Uploading Files...");

        String bookUid = firebaseBookAuth.getUid();
        String authorUid = firebaseAuthorAuth.getUid();

        HashMap<String, Object> bookHashMap = new HashMap<>();
        HashMap<String, Object> authorHashMap = new HashMap<>();

        bookHashMap.put("uid", bookUid);
        bookHashMap.put("id", "" + timestamp);
        bookHashMap.put("bookName", bookName);
        bookHashMap.put("authorName", authorName);
        bookHashMap.put("genreId", selectedGenreId);
        bookHashMap.put("year", year);
        bookHashMap.put("pages", pages);
        bookHashMap.put("chapters", chapters);
        bookHashMap.put("description", bookDescription);
        bookHashMap.put("pdfUrl", uploadPdfUrl);
        bookHashMap.put("imageUrl", uploadImageUrl);
        bookHashMap.put("timestamp", timestamp);
        bookHashMap.put("uploadedBy", userEmail);

        authorHashMap.put("uid", authorUid);
        authorHashMap.put("authorName", authorName);
        authorHashMap.put("authorEmail", authorEmail);
        authorHashMap.put("aboutAuthor", aboutAuthor);

        refBook = FirebaseDatabase.getInstance().getReference("Books");
        refBook.child("" + timestamp)
                .setValue(bookHashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: Successfully uploaded book data...");
                    Toast.makeText(getContext(), "Successfully uploaded book data....", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: Failed to upload due to " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to upload due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        refAuthor = FirebaseDatabase.getInstance().getReference("Authors");
        checkAuthorExists(authorEmail, exists -> {
            if (exists) {
                Toast.makeText(getContext(), "Author found Successfully", Toast.LENGTH_SHORT).show();
            } else {
                refAuthor.child("" + timestamp)
                        .setValue(authorHashMap)
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            Log.d(TAG, "onSuccess: Successfully uploaded author data...");
                            Toast.makeText(getContext(), "Successfully uploaded author data...", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Log.e(TAG, "onFailure: Failed to upload due to " + e.getMessage());
                            Toast.makeText(getContext(), "Failed to upload due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.requireNonNull(uri.getScheme()).equals("content")) {
            try (Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name from URI", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void checkBookExists(String bookName, OnCheckBookExistsListener listener) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        booksRef.orderByChild("bookName").equalTo(bookName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listener.onCheckBookExists(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking book existence", error.toException());
                    }
                });
    }

    private void checkAuthorExists(String authorEmail, OnCheckAuthorExistsListener listener) {
        DatabaseReference authorsRef = FirebaseDatabase.getInstance().getReference("Authors");
        authorsRef.orderByChild("authorEmail").equalTo(authorEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listener.onCheckAuthorExists(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking author existence", error.toException());
                    }
                });
    }

    interface OnCheckBookExistsListener {
        void onCheckBookExists(boolean exists);
    }

    interface OnCheckAuthorExistsListener {
        void onCheckAuthorExists(boolean exists);
    }

    private void setError(TextInputEditText editText, int drawableResId) {
        editText.setBackgroundResource(drawableResId);
    }

    // Method to clear error on TextInputLayout
    private void clearError(TextInputLayout layout) {
        layout.setError(null);
    }
}
