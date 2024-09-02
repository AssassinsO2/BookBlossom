package com.example.bookblossom.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookblossom.models.Book;
import com.example.bookblossom.GenreUtil;
import com.example.bookblossom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.ViewHolder> {

    private final Context context;
    private List<Book> bookList;
    private static final String TAG = "MyBookAdapter";

    public MyBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.bookName.setText(book.getBookName());
        holder.bookAuthor.setText(book.getAuthorName());
        String genreName = GenreUtil.getGenreName(book.getGenreId());
        holder.bookGenre.setText(genreName);

        // Show loading indicator
        holder.loadingIndicator.setVisibility(View.VISIBLE);

        // Set book image from cache or download if not available
        Picasso.get()
                .load(book.getImageUrl())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.book_image_background) // Optional placeholder
                .error(R.drawable.book_image_background) // Optional error drawable
                .into(holder.bookImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Hide loading indicator once image is loaded
                        holder.loadingIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Hide loading indicator if loading fails
                        holder.loadingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "Image loading failed: " + (e != null ? e.getMessage() : "Unknown error"));
                    }
                });

        holder.buttonViewPdf.setOnClickListener(v -> openPdf(book.getPdfUrl()));
        holder.buttonDelete.setOnClickListener(v -> deleteBook(book, position));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView bookName, bookAuthor, bookGenre;
        ImageView bookImageView;
        Button buttonDelete, buttonEdit, buttonViewPdf;
        ProgressBar loadingIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookName = itemView.findViewById(R.id.book_name);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookGenre = itemView.findViewById(R.id.book_genre);
            bookImageView = itemView.findViewById(R.id.book_image_button);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonViewPdf = itemView.findViewById(R.id.button_view_pdf);
            loadingIndicator = itemView.findViewById(R.id.loading_indicator);
        }
    }

    private void deleteBook(Book book, int position) {
        // Get references to Firebase Database and Storage
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Books").child(book.getId());
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Delete book data from Firebase Database
        databaseRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Delete book image from Firebase Storage
                    StorageReference imageRef = storage.getReferenceFromUrl(book.getImageUrl());
                    imageRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "Image deleted successfully");

                                // Delete book PDF from Firebase Storage
                                StorageReference pdfRef = storage.getReferenceFromUrl(book.getPdfUrl());
                                pdfRef.delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Log.d(TAG, "PDF deleted successfully");

                                            // Book, image, and PDF deleted successfully
                                            Toast.makeText(context, "Book and associated files deleted successfully", Toast.LENGTH_SHORT).show();

                                            // Remove the book from all users' favorites
                                            removeBookFromFavorites(book.getId());

                                            // Remove the item from the RecyclerView's data source on the main thread
                                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                                bookList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, bookList.size());
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to delete PDF: " + e.getMessage());
                                            Toast.makeText(context, "Failed to delete PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete image: " + e.getMessage());
                                Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete book: " + e.getMessage());
                    Toast.makeText(context, "Failed to delete book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeBookFromFavorites(String bookId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child(userId).child("Favourites").child(bookId);
                        favoriteRef.removeValue()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Book removed from user's favorites for user: " + userId))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove book from user's favorites for user: " + userId + ", Error: " + e.getMessage()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error querying users for removing book from favorites: " + databaseError.getMessage());
            }
        });
    }

    private void openPdf(String pdfUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(context, "No application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBooks(List<Book> books) {
        this.bookList = books;
        notifyDataSetChanged();
    }
}
