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

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bookblossom.adapters.BookAdapter;
import com.example.bookblossom.adapters.GenreAdapter;
import com.example.bookblossom.models.Book;
import com.example.bookblossom.models.HomeViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private GenreAdapter genreAdapter;
    private BookAdapter bookAdapter;
    private String selectedGenreId = ""; // Track selected genre ID
    private final List<Book> allBooks = new ArrayList<>();
    private RecyclerView recyclerViewBooks;
    private String userEmail, userName, userImage = null;
    private final List<String> imageUrls = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        AppUpdateService pAppSvc = new AppUpdateService();
        pAppSvc.DoUpdate(getContext());

        // Initialize ViewModel
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // Fetch Genre names
        GenreUtil.fetchGenreNames();
        // Initialize the RecyclerView for books
        recyclerViewBooks = view.findViewById(R.id.recycler_view_books);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get user details from arguments
        assert getArguments() != null;
        userEmail = getArguments().getString("userEmail");
        userName = getArguments().getString("userName");
        userImage = getArguments().getString("userImage");

        // Initialize the RecyclerView for categories
        RecyclerView recyclerViewCategory = view.findViewById(R.id.recycler_view_category);
        recyclerViewCategory.setLayoutManager(new GridLayoutManager(getContext(), 3));
        genreAdapter = new GenreAdapter(getContext(), new ArrayList<>());
        recyclerViewCategory.setAdapter(genreAdapter);

        // Find the profile button and set click listener
        ImageButton profileButton = view.findViewById(R.id.profile);
        profileButton.setOnClickListener(v -> showProfileDialog());

        // Load user image into profile button
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

        // Observe books LiveData
        homeViewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
            allBooks.clear();
            allBooks.addAll(books);
            imageUrls.clear();
            for (Book book : allBooks) {
                imageUrls.add(book.getImageUrl());
            }
            preloadImages(imageUrls);
            if (bookAdapter == null) {
                bookAdapter = new BookAdapter(getContext(), allBooks);
                recyclerViewBooks.setAdapter(bookAdapter);
            } else {
                bookAdapter.notifyItemRangeInserted(0, allBooks.size());
            }
            Log.d(TAG, "Fetched " + allBooks.size() + " books from ViewModel.");
        });

        // Observe genres LiveData
        homeViewModel.getGenres().observe(getViewLifecycleOwner(), genres -> genreAdapter.updateGenres(genres));

        // Set click listener for genres
        genreAdapter.setOnItemClickListener(genreId -> {
            Log.d(TAG, "Clicked genreId: " + genreId);
            selectedGenreId = genreId;
            filterBooksByGenre();
        });

        // Initialize search bar and add text change listener
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

        return view;
    }

    // Preload images using Picasso
    private void preloadImages(List<String> imageUrls) {
        for (String url : imageUrls) {
            Picasso.get().load(url).fetch(); // This will preload the images
        }
    }

    // Show profile dialog
    private void showProfileDialog() {
        ProfileDialog dialog = new ProfileDialog(getContext(), userEmail, userName, userImage);
        dialog.show();
    }

    // Filter books by selected genre and update RecyclerView
    private void filterBooksByGenre() {
        ArrayList<Book> filteredBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.getGenreId().equals(selectedGenreId)) {
                filteredBooks.add(book);
            }
        }
        updateBookRecyclerView(filteredBooks);
    }

    // Filter books by search query and update RecyclerView
    private void filterBooks(String query) {
        ArrayList<Book> filteredBooks = new ArrayList<>();
        query = query.toLowerCase();

        for (Book book : allBooks) {
            if (book.getBookName().toLowerCase().contains(query) || book.getAuthorName().toLowerCase().contains(query)) {
                filteredBooks.add(book);
            }
        }
        updateBookRecyclerView(filteredBooks);
    }

    // Update RecyclerView with filtered books
    private void updateBookRecyclerView(ArrayList<Book> filteredBooks) {
        bookAdapter.updateBooks(filteredBooks);
    }
}
