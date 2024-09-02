package com.example.bookblossom.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookblossom.models.Book;
import com.example.bookblossom.BookBlossom;
import com.example.bookblossom.DetailedBook;
import com.example.bookblossom.GenreUtil;
import com.example.bookblossom.R;
import com.example.bookblossom.databinding.RowToReadBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ToReadBookAdapter extends RecyclerView.Adapter<ToReadBookAdapter.HolderToRead> {

    private static final String TAG = "ToReadBookAdapter";
    private final Context context;
    private ArrayList<Book> bookList;
    private RowToReadBinding binding;

    public ToReadBookAdapter(Context context, ArrayList<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public HolderToRead onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowToReadBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderToRead(binding.getRoot());
    }

    public void onBindViewHolder(@NonNull HolderToRead holder, int position) {
        Book loadedBook = bookList.get(position);
        loadBookDetails(loadedBook, holder);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedBook.class);
            intent.putExtra("id", loadedBook.getId());
            intent.putExtra("bookName", loadedBook.getBookName());
            intent.putExtra("authorName", loadedBook.getAuthorName());
            intent.putExtra("genre", loadedBook.getGenreId());
            intent.putExtra("chapters", loadedBook.getChapters());
            intent.putExtra("year", loadedBook.getYear());
            intent.putExtra("pages", loadedBook.getPages());
            intent.putExtra("description", loadedBook.getDescription());
            intent.putExtra("imageUrl", loadedBook.getImageUrl());
            intent.putExtra("pdfUrl", loadedBook.getPdfUrl()); // Pass the PDF URL
            context.startActivity(intent);
        });

        holder.removeToReadBtn.setOnClickListener(v -> BookBlossom.removeFromToRead(context, loadedBook.getId()));
    }

    private void loadBookDetails(Book book, HolderToRead holder) {
        String bookId = book.getId();

        Log.e(TAG, "loadBookDetails: Book details of book ID " + bookId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Book loadedBook = snapshot.getValue(Book.class);
                        if (loadedBook != null) {
                            // Update the book object
                            book.setBookName(loadedBook.getBookName());
                            book.setDescription(loadedBook.getDescription());
                            book.setAuthorName(loadedBook.getAuthorName());
                            String genreName = GenreUtil.getGenreName(loadedBook.getGenreId());
                            book.setGenreId(genreName);
                            book.setImageUrl(loadedBook.getImageUrl());
                            book.setChapters(loadedBook.getChapters()); // Update chapters
                            book.setPages(loadedBook.getPages()); // Update pages
                            book.setYear(loadedBook.getYear()); // Update year

                            holder.titleTv.setText(loadedBook.getBookName());
                            holder.descriptionTv.setText(loadedBook.getDescription());
                            holder.genreTv.setText(GenreUtil.getGenreName(loadedBook.getGenreId()));
                            holder.authorTv.setText(loadedBook.getAuthorName());

                            // Load the image using Glide
                            Glide.with(context)
                                    .load(loadedBook.getImageUrl())
                                    .placeholder(R.drawable.ic_book_placeholder) // Optional placeholder
                                    .into(holder.pdfImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load book details", error.toException());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class HolderToRead extends RecyclerView.ViewHolder {

        ImageButton removeToReadBtn;
        ImageView pdfImage;
        TextView titleTv, authorTv, genreTv, descriptionTv;

        public HolderToRead(@NonNull View itemView) {
            super(itemView);

            pdfImage = binding.pdfImage;
            removeToReadBtn = binding.removeToReadBtn;
            titleTv = binding.titleTv;
            authorTv = binding.authorTv;
            genreTv = binding.genreTv;
            descriptionTv = binding.descriptionTv;
        }
    }

    public void updateBooks(ArrayList<Book> updatedBooks) {
        bookList = updatedBooks;
        notifyDataSetChanged();
    }
}
