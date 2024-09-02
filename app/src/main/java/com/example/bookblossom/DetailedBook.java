package com.example.bookblossom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.bookblossom.adapters.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class DetailedBook extends AppCompatActivity {

    private static final String TAG = "DetailedBook";
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_book);

        imageView = findViewById(R.id.imageView);  // Initialize ImageView

        ViewPager viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());

        Intent intent = getIntent();
        String bookId = intent.getStringExtra("id");
        String bookName = intent.getStringExtra("bookName");
        String authorName = intent.getStringExtra("authorName");
        String genre = intent.getStringExtra("genre");
        String chapters = intent.getStringExtra("chapters");
        String year = intent.getStringExtra("year");
        String pages = intent.getStringExtra("pages");
        String description = intent.getStringExtra("description");
        String pdfUrl = intent.getStringExtra("pdfUrl");
        String imageUrl = intent.getStringExtra("imageUrl");

        Log.d(TAG, "BOOK ID: " + bookId);

        // Load image using Glide
        Picasso.get()
                .load(imageUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView);

        // Pass the book details to AboutBookFragment and ReviewsFragment
        Bundle bundle = new Bundle();
        bundle.putString("bookId", bookId);
        bundle.putString("bookName", bookName);
        bundle.putString("authorName", authorName);
        bundle.putString("genre", genre);
        bundle.putString("chapters", chapters);
        bundle.putString("year", year);
        bundle.putString("pages", pages);
        bundle.putString("description", description);
        bundle.putString("pdfUrl", pdfUrl);

        AboutBookFragment aboutBookFragment = new AboutBookFragment();
        aboutBookFragment.setArguments(bundle);

        ReviewsFragment reviewsFragment = new ReviewsFragment();
        reviewsFragment.setArguments(bundle);

        adapter.addFragment(aboutBookFragment, "About");
        adapter.addFragment(reviewsFragment, "Reviews");

        viewPager.setAdapter(adapter);
    }
}
