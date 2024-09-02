package com.example.bookblossom;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookblossom.adapters.ReviewAdapter;
import com.example.bookblossom.databinding.DialogCommentAddBinding;
import com.example.bookblossom.databinding.FragmentReviewsBinding;
import com.example.bookblossom.models.Review;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ReviewsFragment extends Fragment {

    private static final String TAG = "ReviewsFragment";
    private FragmentReviewsBinding binding;
    FirebaseAuth firebaseAuth;
    Bundle args;
    String bookId;
    private ProgressDialog progressDialog;
    TextInputLayout reviewInputLayout;
    TextInputEditText inputReview;
    private ArrayList<Review> reviewArrayList;
    private ReviewAdapter reviewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        args = getArguments();
        firebaseAuth = FirebaseAuth.getInstance();
        bookId = args != null ? args.getString("bookId") : null;
        Log.d(TAG, "BOOK ID: " + bookId);

        loadReviews();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.addReviewBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "You are not logged in...", Toast.LENGTH_SHORT).show();
            }
            else {
                addReviewDialog();
            }
        });

        return view;
    }

    private void loadReviews() {

        reviewArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Reviews")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Review review1 = dataSnapshot.getValue(Review.class);
                            reviewArrayList.add(review1);
                        }

                        reviewAdapter = new ReviewAdapter(getContext(), reviewArrayList);
                        binding.reviewsRv.setAdapter(reviewAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String review = "";
    private void addReviewDialog() {
        DialogCommentAddBinding reviewAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(getContext()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ReviewDialog);
        builder.setView(reviewAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        reviewAddBinding.submitBtn.setOnClickListener(v -> {
            inputReview = reviewAddBinding.reviewEt;
            review = Objects.requireNonNull(inputReview.getText()).toString().trim();
            reviewInputLayout = reviewAddBinding.reviewTil;
            if(review.isEmpty())
            {
                reviewInputLayout.setHelperText("Enter your review");
                setError(inputReview, R.drawable.outlined_error_text_view);
                Toast.makeText(getContext(), "Enter your review...", Toast.LENGTH_SHORT).show();
            }
            else {
                reviewInputLayout.setHelperText(null);
                setError(inputReview, R.drawable.outlined_text_view);
                clearError(reviewInputLayout);
                alertDialog.dismiss();
                checkIfReviewExists();
            }
        });
    }

    private void checkIfReviewExists() {
        showProgressDialog("Checking for existing review...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("Reviews");
        Query query = ref.orderByChild("uid").equalTo(firebaseAuth.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "You have already added a review for this book.", Toast.LENGTH_SHORT).show();
                } else {
                    addReview();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Failed to check existing review: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addReview() {
        showProgressDialog("Adding Review...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", timestamp);
        hashMap.put("bookId", bookId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("review", review);
        hashMap.put("uid", firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Reviews").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Review Added", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                    loadReviews(); // Refresh the reviews after adding a new one
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "Failed to add review due to" +e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setError(TextInputEditText editText, int drawableResId) {
        editText.setBackgroundResource(drawableResId);
    }

    private void clearError(TextInputLayout layout) {
        layout.setError(null);
    }
}
