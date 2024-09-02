package com.example.bookblossom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bookblossom.adapters.FavoritesBookAdapter;
import com.example.bookblossom.adapters.CurrentlyReadingAdapter;
import com.example.bookblossom.adapters.ToReadBookAdapter;
import com.example.bookblossom.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class BookmarksFragment extends Fragment {

    private static final String TAG = "BookmarksFragment";
    private ArrayList<Book> FavouriteBooks, ToReadBooks, CurrentlyReading;
    RecyclerView recyclerViewFavouriteBooks, recyclerViewToReadBooks, recyclerViewCurrentlyReadingBooks;
    private FavoritesBookAdapter favouriteBooksAdapter;
    private ToReadBookAdapter toReadBooksAdapter;
    private CurrentlyReadingAdapter currentlyReadingAdapter;
    FirebaseAuth firebaseAuth;
    View view;
    String userEmail, userName, userImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        // Retrieve user email and name from arguments
        assert getArguments() != null;
        userEmail = getArguments().getString("userEmail");
        userName = getArguments().getString("userName");
        userImage = getArguments().getString("userImage");

        // Find the profile button
        ImageButton profileButton = view.findViewById(R.id.profile);
        profileButton.setOnClickListener(v -> showProfileDialog());

        if (userImage != null && !userImage.isEmpty()) {
            Glide.with(requireContext())
                    .load(userImage)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.account_circle_black)
                    .error(R.drawable.account_circle_black)
                    .into(profileButton);
        } else {
            profileButton.setImageResource(R.drawable.account_circle_black);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerViews for each section with separate LayoutManagers
        recyclerViewFavouriteBooks = view.findViewById(R.id.favouriteRv);
        recyclerViewToReadBooks = view.findViewById(R.id.recycler_view_to_read_books);
        recyclerViewCurrentlyReadingBooks = view.findViewById(R.id.recycler_view_currently_reading_books);

        FavouriteBooks = new ArrayList<>();
        favouriteBooksAdapter = new FavoritesBookAdapter(getContext(), FavouriteBooks);
        recyclerViewFavouriteBooks.setAdapter(favouriteBooksAdapter);

        ToReadBooks = new ArrayList<>();
        toReadBooksAdapter = new ToReadBookAdapter(getContext(), ToReadBooks);
        recyclerViewToReadBooks.setAdapter(toReadBooksAdapter);

        CurrentlyReading = new ArrayList<>();
        currentlyReadingAdapter = new CurrentlyReadingAdapter(getContext(), CurrentlyReading);
        recyclerViewCurrentlyReadingBooks.setAdapter(currentlyReadingAdapter);

        // Set up layout managers for horizontal scrolling
        LinearLayoutManager layoutManagerHorizontal1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerHorizontal2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerHorizontal3 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerViewFavouriteBooks.setLayoutManager(layoutManagerHorizontal1);
        recyclerViewToReadBooks.setLayoutManager(layoutManagerHorizontal2);
        recyclerViewCurrentlyReadingBooks.setLayoutManager(layoutManagerHorizontal3);

        EditText searchBar = view.findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().trim();
                Log.d(TAG, "Search query changed: " + query);
                filterBooks(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        loadFavouritesBooks();  // Load favorite books after setting up the adapter and RecyclerView
        loadToReadBooks();  // Load books to read
        loadCurrentlyReadingBooks();  // Load currently reading books
        return view;
    }

    private void loadFavouritesBooks() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Favourites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FavouriteBooks.clear();
                        for (DataSnapshot snapshot1: snapshot.getChildren()) {
                            String bookId = ""+snapshot1.child("bookId").getValue();

                            Book newBook = new Book();
                            newBook.setId(bookId);

                            FavouriteBooks.add(newBook);
                        }
                        favouriteBooksAdapter.updateBooks(FavouriteBooks);  // Notify adapter about data changes
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load favourite books", error.toException());
                    }
                });
    }

    private void loadToReadBooks() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("ToRead")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ToReadBooks.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            String bookId = snapshot1.child("bookId").getValue(String.class);
                            Log.d(TAG, "Loaded bookId: " + bookId);

                            if (bookId != null) {
                                Book newBook = new Book();
                                newBook.setId(bookId);
                                ToReadBooks.add(newBook);
                            }
                        }
                        toReadBooksAdapter.updateBooks(ToReadBooks);  // Notify adapter about data changes
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load To Read books", error.toException());
                    }
                });
    }

    private void loadCurrentlyReadingBooks() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("CurrentlyReading")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        CurrentlyReading.clear();
                        for (DataSnapshot snapshot1: snapshot.getChildren()) {
                            String bookId = "" + snapshot1.child("bookId").getValue();

                            Book newBook = new Book();
                            newBook.setId(bookId);

                            CurrentlyReading.add(newBook);
                        }
                        currentlyReadingAdapter.updateBooks(CurrentlyReading);  // Notify adapter about data changes
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load Currently Reading books", error.toException());
                    }
                });
    }

    // Method to filter books by search query
    private void filterBooks(String query) {
        filterList(FavouriteBooks, favouriteBooksAdapter, query);
        filterList(ToReadBooks, toReadBooksAdapter, query);
        filterList(CurrentlyReading, currentlyReadingAdapter, query);
    }

    // Helper method to filter a list of books and update the adapter
    private void filterList(ArrayList<Book> bookList, RecyclerView.Adapter<?> adapter, String query) {
        ArrayList<Book> filteredBooks = new ArrayList<>();
        query = query.toLowerCase();

        for (Book book : bookList) {
            String bookName = book.getBookName();
            String authorName = book.getAuthorName();

            if (bookName.toLowerCase().contains(query)) {
                filteredBooks.add(book);
            } else if (authorName.toLowerCase().contains(query)) {
                filteredBooks.add(book);
            }
        }

        requireActivity().runOnUiThread(() -> updateBookRecyclerView(adapter, filteredBooks));
    }

    // Helper method to update RecyclerView with filtered books
    private void updateBookRecyclerView(RecyclerView.Adapter<?> adapter, ArrayList<Book> filteredBooks) {
        if (adapter instanceof FavoritesBookAdapter) {
            FavoritesBookAdapter bookmarkBookAdapter = new FavoritesBookAdapter(getContext(), filteredBooks);
            recyclerViewFavouriteBooks.setAdapter(bookmarkBookAdapter);
            bookmarkBookAdapter.updateBooks(filteredBooks);
        } else if (adapter instanceof ToReadBookAdapter) {
            ToReadBookAdapter toReadBookAdapter = new ToReadBookAdapter(getContext(), filteredBooks);
            recyclerViewToReadBooks.setAdapter(toReadBookAdapter);
            toReadBookAdapter.updateBooks(filteredBooks);
        } else if (adapter instanceof CurrentlyReadingAdapter) {
            CurrentlyReadingAdapter currentlyReadingAdapter = new CurrentlyReadingAdapter(getContext(), filteredBooks);
            recyclerViewCurrentlyReadingBooks.setAdapter(currentlyReadingAdapter);
            currentlyReadingAdapter.updateBooks(filteredBooks);
        }
    }

    private void showProfileDialog() {
        // Create and show the custom dialog
        ProfileDialog dialog = new ProfileDialog(getContext(), userEmail, userName, userImage);
        dialog.show();
    }
}
