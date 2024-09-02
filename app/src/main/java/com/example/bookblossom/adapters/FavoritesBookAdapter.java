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
import com.example.bookblossom.databinding.RowFavouriteBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoritesBookAdapter extends RecyclerView.Adapter<FavoritesBookAdapter.HolderFavourite> {

    private static final String TAG = "BookmarkBookAdapter";
    private final Context context;
    private ArrayList<Book> bookList;
    private RowFavouriteBinding binding;

    public FavoritesBookAdapter(Context context, ArrayList<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public HolderFavourite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowFavouriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderFavourite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFavourite holder, int position) {
        Book book = bookList.get(position);
        loadBookDetails(book, holder);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedBook.class);
            intent.putExtra("id", book.getId());
            intent.putExtra("bookName", book.getBookName());
            intent.putExtra("authorName", book.getAuthorName());
            intent.putExtra("genre", book.getGenreId());
            intent.putExtra("chapters", book.getChapters());
            intent.putExtra("year", book.getYear());
            intent.putExtra("pages", book.getPages());
            intent.putExtra("description", book.getDescription());
            intent.putExtra("imageUrl", book.getImageUrl());
            intent.putExtra("pdfUrl", book.getPdfUrl()); // Pass the PDF URL
            context.startActivity(intent);
        });

        holder.removeFavBtn.setOnClickListener(v -> BookBlossom.removeFromFavorites(context, book.getId()));
    }

    private void loadBookDetails(Book book, HolderFavourite holder) {
        String bookId = book.getId();

        Log.d(TAG, "Loading details for book ID: " + bookId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book loadedBook = snapshot.getValue(Book.class);
                if (loadedBook != null) {
                    book.setBookName(loadedBook.getBookName());
                    book.setDescription(loadedBook.getDescription());
                    book.setAuthorName(loadedBook.getAuthorName());
                    book.setGenreId(GenreUtil.getGenreName(loadedBook.getGenreId()));
                    book.setImageUrl(loadedBook.getImageUrl());
                    book.setChapters(loadedBook.getChapters());
                    book.setPages(loadedBook.getPages());
                    book.setYear(loadedBook.getYear());

                    holder.titleTv.setText(loadedBook.getBookName());
                    holder.descriptionTv.setText(loadedBook.getDescription());
                    holder.genreTv.setText(GenreUtil.getGenreName(loadedBook.getGenreId()));
                    holder.authorTv.setText(loadedBook.getAuthorName());

                    Glide.with(context)
                            .load(loadedBook.getImageUrl())
                            .placeholder(R.drawable.ic_book_placeholder)
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

    public class HolderFavourite extends RecyclerView.ViewHolder {

        ImageButton removeFavBtn;
        ImageView pdfImage;
        TextView titleTv, authorTv, genreTv, descriptionTv;

        public HolderFavourite(@NonNull View itemView) {
            super(itemView);

            pdfImage = binding.pdfImage;
            removeFavBtn = binding.removeFavBtn;
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
