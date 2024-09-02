package com.example.bookblossom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bookblossom.adapters.MyBookAdapter;
import com.example.bookblossom.models.Book;
import com.example.bookblossom.models.MyBooksViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class MyBooksFragment extends Fragment {

    private static final String TAG = "MyBooksFragment";

    private MyBookAdapter bookAdapter;
    private ArrayList<Book> bookList;
    private DatabaseReference mBooksDatabase;
    private MyBooksViewModel myBooksViewModel;
    private String userEmail, userName, userImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_books, container, false);

        // Initialize ViewModel
        myBooksViewModel = new ViewModelProvider(this).get(MyBooksViewModel.class);

        // Retrieve user information from arguments
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
            userName = getArguments().getString("userName");
            userImage = getArguments().getString("userImage");
        }

        // Initialize UI elements
        ImageButton profileButton = view.findViewById(R.id.profile);
        Button addBookButton = view.findViewById(R.id.add_book_button);
        EditText searchBar = view.findViewById(R.id.search_bar);
        RecyclerView recyclerViewBooks = view.findViewById(R.id.recycler_view_books);

        // Set up RecyclerView
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        bookAdapter = new MyBookAdapter(getContext(), new ArrayList<>());
        recyclerViewBooks.setAdapter(bookAdapter);

        // Initialize Firebase database reference
        mBooksDatabase = FirebaseDatabase.getInstance().getReference().child("Books");

        // Fetch books from Firebase
        fetchBooksFromFirebase();

        // Set up profile button
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

        profileButton.setOnClickListener(v -> showProfileDialog());
        addBookButton.setOnClickListener(v -> addBook());

        // Set up search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().trim();
                Log.d(TAG, "Search query changed: " + query);
                filterBooks(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Observe changes in books LiveData
        myBooksViewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
            bookList = books;
            bookAdapter.updateBooks(bookList); // Notify adapter of data changes
        });

        return view;
    }

    private void fetchBooksFromFirebase() {
        mBooksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Book> newBookList = new ArrayList<>();
                ArrayList<String> imageUrls = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null && Objects.equals(book.getUploadedBy(), userEmail)) {
                        newBookList.add(book);
                        imageUrls.add(book.getImageUrl()); // Collect image URLs
                    }
                }
                preloadImages(imageUrls); // Preload images before updating ViewModel
                myBooksViewModel.setBooks(newBookList); // Update ViewModel with new data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read books: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load books", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadImages(ArrayList<String> imageUrls) {
        for (String url : imageUrls) {
            Picasso.get().load(url).fetch(); // This will preload the images
        }
    }

    private void filterBooks(String query) {
        ArrayList<Book> filteredBooks = new ArrayList<>();
        query = query.toLowerCase();

        for (Book book : bookList) {
            if (book.getBookName().toLowerCase().contains(query) ||
                    book.getAuthorName().toLowerCase().contains(query)) {
                filteredBooks.add(book);
            }
        }
        updateBookRecyclerView(filteredBooks);
    }

    private void addBook() {
        // Navigate to AddBookFragment
        AddBookFragment addBookFragment = new AddBookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userEmail", userEmail);
        addBookFragment.setArguments(bundle);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addBookFragment);
        transaction.addToBackStack(null); // Optional: Add to back stack so user can navigate back
        transaction.commit();
    }

    private void showProfileDialog() {
        // Create and show the custom dialog
        ProfileDialog dialog = new ProfileDialog(getContext(), userEmail, userName, userImage);
        dialog.show();
    }

    private void updateBookRecyclerView(ArrayList<Book> filteredBooks) {
        bookAdapter.updateBooks(filteredBooks); // Ensure `updateBooks` method in `MyBookAdapter` is implemented
    }
}
