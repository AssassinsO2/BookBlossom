package com.example.bookblossom.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookblossom.BookBlossom;
import com.example.bookblossom.R;
import com.example.bookblossom.models.Review;
import com.example.bookblossom.databinding.RowReviewBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.HolderReview> {

    private final Context context;
    private final ArrayList<Review> reviewArrayList;
    private final FirebaseAuth firebaseAuth;

    public ReviewAdapter(Context context, ArrayList<Review> reviewList) {
        this.context = context;
        this.reviewArrayList = reviewList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        com.example.bookblossom.databinding.RowReviewBinding binding = RowReviewBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderReview(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        Review review = reviewArrayList.get(position);
        String userReview = review.getReview();
        String uid = review.getUid();
        String timestamp = review.getTimestamp();

        String date = BookBlossom.formatTimeStamp(Long.parseLong(timestamp));

        holder.dateTv.setText(date);
        holder.reviewTv.setText(userReview);

        loadUserDetails(review, holder);

        holder.itemView.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())) {
                deleteReview(review, holder);
            }
        });
    }

    private void deleteReview(Review review, HolderReview holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                    reference.child(review.getBookId())
                            .child("Reviews")
                            .child(review.getId())
                            .removeValue()
                            .addOnSuccessListener(v -> {
                                int position = holder.getAdapterPosition();
                                reviewArrayList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, reviewArrayList.size());
                                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete review due to " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadUserDetails(Review review, HolderReview holder) {
        String uid = review.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String displayName = "" + snapshot.child("displayName").getValue();
                String imageUrl = "" + snapshot.child("imageUrl").getValue();
                holder.nameTv.setText(displayName);

                try {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.account_circle_black)
                            .into(holder.profileTv);
                } catch (Exception e) {
                    holder.profileTv.setImageResource(R.drawable.account_circle_black);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    public static class HolderReview extends RecyclerView.ViewHolder {

        ShapeableImageView profileTv;
        TextView nameTv, dateTv, reviewTv;

        public HolderReview(@NonNull View itemView, RowReviewBinding binding) {
            super(itemView);
            profileTv = binding.profileTv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            reviewTv = binding.reviewTv;
        }
    }

    public void updateData(ArrayList<Review> newReviews) {
        this.reviewArrayList.clear();
        this.reviewArrayList.addAll(newReviews);
        notifyDataSetChanged();
    }
}
