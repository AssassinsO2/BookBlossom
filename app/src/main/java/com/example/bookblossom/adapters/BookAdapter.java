package com.example.bookblossom.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookblossom.databinding.RowBookBinding;
import com.example.bookblossom.models.Book;
import com.example.bookblossom.BookBlossom;
import com.example.bookblossom.DetailedBook;
import com.example.bookblossom.GenreUtil;
import com.example.bookblossom.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private static final String TAG = "BookAdapter";
    private final Context context;
    private List<Book> bookList;
    private final FirebaseAuth firebaseAuth;

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowBookBinding binding = RowBookBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookName, bookAuthor, bookGenre, bookDescription;
        private final ImageButton bookImageView, addFavourite, addToRead, addToCurrentlyReading;
        private final ProgressBar loadingIndicator;
        private boolean isInMyFavourite = false;
        private boolean isInToRead = false;
        private boolean isInCurrentlyReading = false;

        public ViewHolder(@NonNull RowBookBinding binding) {
            super(binding.getRoot());

            bookName = binding.titleTv;
            bookAuthor = binding.authorTv;
            bookGenre = binding.genreTv;
            bookDescription = binding.descriptionTv;
            bookImageView = binding.pdfImage;
            addFavourite = binding.addFavourite;
            addToRead = binding.toRead;
            addToCurrentlyReading = binding.currentlyReading;
            loadingIndicator = binding.loadingIndicator;
        }

        public void bind(Book book) {
            String bookImage = book.getImageUrl();
            bookName.setText(book.getBookName());
            bookAuthor.setText(book.getAuthorName());
            String genreName = GenreUtil.getGenreName(book.getGenreId());
            bookGenre.setText(genreName);
            bookDescription.setText(book.getDescription());

            // Show loading indicator
            loadingIndicator.setVisibility(View.VISIBLE);

            // Set book image from cache or download if not available
            Picasso.get()
                    .load(bookImage)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.book_image_background)
                    .error(R.drawable.book_image_background)
                    .into(bookImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            loadingIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Failed to load image: " + book.getImageUrl(), e);
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    });

            if (firebaseAuth.getCurrentUser() != null) {
                checkIsFavourite(book.getId());
                checkIsInToRead(book.getId());
                checkIsInCurrentlyReading(book.getId());
            }

            addFavourite.setOnClickListener(v -> handleFavouriteClick(book.getId()));
            addToRead.setOnClickListener(v -> handleToReadClick(book.getId()));
            addToCurrentlyReading.setOnClickListener(v -> handleCurrentlyReadingClick(book.getId()));

            itemView.setOnClickListener(v -> openDetailedBookActivity(book));
        }

        private void handleFavouriteClick(String bookId) {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInMyFavourite) {
                BookBlossom.removeFromFavorites(context, bookId);
            } else {
                BookBlossom.addToFavourites(context, bookId);
            }
        }

        private void handleToReadClick(String bookId) {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInToRead) {
                BookBlossom.removeFromToRead(context, bookId);
            } else {
                BookBlossom.addToToRead(context, bookId);
            }
        }

        private void handleCurrentlyReadingClick(String bookId) {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInCurrentlyReading) {
                BookBlossom.removeFromCurrentlyReading(context, bookId);
            } else {
                BookBlossom.addToCurrentlyReading(context, bookId);
            }
        }

        private void openDetailedBookActivity(Book book) {
            Intent intent = new Intent(context, DetailedBook.class);
            intent.putExtra("id", book.getId());
            intent.putExtra("bookName", book.getBookName());
            intent.putExtra("authorName", book.getAuthorName());
            intent.putExtra("genre", GenreUtil.getGenreName(book.getGenreId()));
            intent.putExtra("chapters", book.getChapters());
            intent.putExtra("year", book.getYear());
            intent.putExtra("pages", book.getPages());
            intent.putExtra("description", book.getDescription());
            intent.putExtra("pdfUrl", book.getPdfUrl());
            intent.putExtra("imageUrl", book.getImageUrl());
            context.startActivity(intent);
        }

        private void checkIsFavourite(String bookId) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(Objects.requireNonNull(firebaseAuth.getUid())).child("Favourites").child(bookId);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFavourite = snapshot.exists();
                    isInMyFavourite = isFavourite;
                    addFavourite.setImageResource(isFavourite
                            ? R.drawable.baseline_favorite_24
                            : R.drawable.baseline_favorite_border_24);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to check if book is favourite", error.toException());
                }
            });
        }

        private void checkIsInToRead(String bookId) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(Objects.requireNonNull(firebaseAuth.getUid())).child("ToRead").child(bookId);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isToRead = snapshot.exists();
                    isInToRead = isToRead;
                    addToRead.setImageResource(isToRead
                            ? R.drawable.ic_to_read_white
                            : R.drawable.ic_to_read);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to check if book is in To Read", error.toException());
                }
            });
        }

        private void checkIsInCurrentlyReading(String bookId) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(Objects.requireNonNull(firebaseAuth.getUid())).child("CurrentlyReading").child(bookId);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isCurrentlyReading = snapshot.exists();
                    isInCurrentlyReading = isCurrentlyReading;
                    addToCurrentlyReading.setImageResource(isCurrentlyReading
                            ? R.drawable.ic_reading_now_white
                            : R.drawable.ic_reading_now);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to check if book is in Currently Reading", error.toException());
                }
            });
        }
    }

    public void updateBooks(List<Book> books) {
        bookList = books;
        notifyDataSetChanged();
    }
}
