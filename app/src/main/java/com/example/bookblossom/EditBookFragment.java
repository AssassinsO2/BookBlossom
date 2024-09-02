package com.example.bookblossom;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookblossom.databinding.FragmentAddBookBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EditBookFragment extends Fragment {

    String userEmail;
    private ImageView bookImageView;
    private ProgressDialog progressDialog;
    private Uri pdfUri = null;
    private Uri imageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PDF_REQUEST = 2;
    private static final String TAG = "EditBookFragment";

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

        binding.genreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genrePickDialog();
            }
        });

        buttonSelectImage.setOnClickListener(v -> openGallery());
        buttonSelectPdf.setOnClickListener(v -> openPdfSelector());

        buttonAddBook.setOnClickListener(v -> {
            String bookName = inputBookName.getText().toString().trim();
            String authorName = inputAuthorName.getText().toString().trim();
            String authorEmail = inputAuthorEmail.getText().toString().trim();
            String genreStr = genre.getText().toString().trim();
            String year = inputYear.getText().toString().trim();
            String pages = inputPages.getText().toString().trim();
            String chapters = inputChapters.getText().toString().trim();
            String bookDescription = inputBookDescription.getText().toString().trim();
            String aboutAuthor = inputAboutAuthor.getText().toString().trim();

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
                    setError(inputBookDescription, R.drawable.outlined_error_text_view);
                } else {
                    bookDescriptionInputLayout.setHelperText(null);
                    setError(inputBookDescription, R.drawable.outlined_text_view);
                    clearError(bookDescriptionInputLayout);
                }

                if (aboutAuthor.isEmpty()) {
                    aboutAuthorInputLayout.setHelperText("About Author is required");
                    setError(inputAboutAuthor, R.drawable.outlined_error_text_view);
                } else {
                    aboutAuthorInputLayout.setHelperText(null);
                    setError(inputAboutAuthor, R.drawable.outlined_text_view);
                    clearError(aboutAuthorInputLayout);
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
                    String genreId = "" + dataSnapshot.child("id").getValue(); // Assuming genreId is the key (if structured this way)
                    String genreTitle = "" + dataSnapshot.child("name").getValue();
                    genreTitleArrayList.add(genreTitle);
                    genreIdArrayList.add(genreId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void genrePickDialog() {
        String[] genresArray = genreTitleArrayList.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pick Genre")
                .setItems(genresArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedGenreId = genreIdArrayList.get(which);
                        selectGenreTitle = genreTitleArrayList.get(which);
                        binding.genreTv.setText(selectGenreTitle);
                    }
                }).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openPdfSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                bookImageView.setImageURI(imageUri);
            } else if (requestCode == PICK_PDF_REQUEST && data != null && data.getData() != null) {
                pdfUri = data.getData();
                selectedPdfTextView.setText(getFileName(pdfUri));
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    private void setError(TextInputEditText inputField, int drawableId) {
        inputField.setBackgroundResource(drawableId);
    }

    private void clearError(TextInputLayout inputLayout) {
        inputLayout.setError(null);
    }

    private void checkBookExists(String bookName, final OnCheckBookExistsListener listener) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        booksRef.orderByChild("bookName").equalTo(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                listener.onCheckBookExists(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check if book exists", error.toException());
            }
        });
    }

    private void uploadFilesToStorage(String bookName, String authorName, String authorEmail, String genre, String year, String pages, String chapters, String bookDescription, String aboutAuthor) {
        progressDialog.setMessage("Uploading Book Details...");
        progressDialog.show();

        String filePathAndName = "Books/" + bookName + "_" + firebaseBookAuth.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String uploadedPdfUrl = uriTask.getResult().toString();

                uploadImageToStorage(bookName, uploadedPdfUrl, authorName, authorEmail, genre, year, pages, chapters, bookDescription, aboutAuthor);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToStorage(String bookName, String uploadedPdfUrl, String authorName, String authorEmail, String genre, String year, String pages, String chapters, String bookDescription, String aboutAuthor) {
        progressDialog.setMessage("Uploading Book Image...");
        progressDialog.show();

        String filePathAndName = "BookImages/" + bookName + "_" + firebaseBookAuth.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String uploadedImageUrl = uriTask.getResult().toString();

                uploadBookInfo(uploadedPdfUrl, uploadedImageUrl, bookName, authorName, authorEmail, genre, year, pages, chapters, bookDescription, aboutAuthor);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Image upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadBookInfo(String uploadedPdfUrl, String uploadedImageUrl, String bookName, String authorName, String authorEmail, String genre, String year, String pages, String chapters, String bookDescription, String aboutAuthor) {
        progressDialog.setMessage("Uploading Book Info...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String uid = firebaseBookAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("bookName", bookName);
        hashMap.put("authorName", authorName);
        hashMap.put("authorEmail", authorEmail);
        hashMap.put("genreId", genre);
        hashMap.put("year", year);
        hashMap.put("pages", pages);
        hashMap.put("chapters", chapters);
        hashMap.put("bookDescription", bookDescription);
        hashMap.put("aboutAuthor", aboutAuthor);
        hashMap.put("pdfUrl", uploadedPdfUrl);
        hashMap.put("imageUrl", uploadedImageUrl);
        hashMap.put("uid", uid);
        hashMap.put("timestamp", timestamp);

        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        booksRef.child(bookName).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Book uploaded successfully", Toast.LENGTH_SHORT).show();
                resetFields();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to upload book due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetFields() {
        binding.bookName.setText("");
        binding.authorName.setText("");
        binding.authorEmail.setText("");
        binding.genreTv.setText("");
        binding.year.setText("");
        binding.pages.setText("");
        binding.chapters.setText("");
        binding.bookDescription.setText("");
        binding.aboutAuthor.setText("");
        binding.bookImageView.setImageResource(R.drawable.ic_book_placeholder);
        binding.selectedPdfTextView.setText("");
        pdfUri = null;
        imageUri = null;
    }

    interface OnCheckBookExistsListener {
        void onCheckBookExists(boolean exists);
    }
}
