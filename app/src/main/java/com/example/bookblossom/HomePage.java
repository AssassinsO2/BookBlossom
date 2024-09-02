package com.example.bookblossom;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.HashMap;
import java.util.Map;

import com.google.android.material.navigation.NavigationView;
import com.example.bookblossom.databinding.ActivityHomePageBinding;

public class HomePage extends AppCompatActivity {

    private static final String TAG = "HomePage";

    private DrawerLayout drawerLayout;
    private String userEmail, userName, userImage;

    // Maps for navigation items
    private final Map<Integer, Class<? extends Fragment>> drawerFragmentMap = new HashMap<Integer, Class<? extends Fragment>>() {{
        put(R.id.nav_authors, AuthorsFragment.class);
        put(R.id.nav_settings, SettingsFragment.class);
        put(R.id.nav_app_info, AppInfoFragment.class);
        put(R.id.nav_send_feedback, FeedbackFragment.class);
    }};

    private final Map<Integer, Class<? extends Fragment>> bottomNavFragmentMap = new HashMap<Integer, Class<? extends Fragment>>() {{
        put(R.id.home, HomeFragment.class);
        put(R.id.my_books, MyBooksFragment.class);
        put(R.id.bookmarks, BookmarksFragment.class);
        put(R.id.menu, null); // Drawer will be opened instead of loading a fragment
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        com.example.bookblossom.databinding.ActivityHomePageBinding binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve user details from the intent
        userEmail = getIntent().getStringExtra("userEmail");
        userName = getIntent().getStringExtra("userName");
        userImage = getIntent().getStringExtra("userImage");

        // Initialize DrawerLayout
        drawerLayout = binding.drawerLayout;

        // Set up NavigationView listener
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);

        // Set up BottomNavigationView listener
        binding.bottomNavigationView.setBackground(null); // Remove default background
        binding.bottomNavigationView.setOnItemSelectedListener(this::handleBottomNavigationItemSelected);

        // Replace the default fragment with HomeFragment
        replaceFragment(new HomeFragment(), userEmail, userName, userImage);

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START); // Close drawer if open
                } else {
                    HomePage.super.getOnBackPressedDispatcher(); // Handle back press normally
                }
            }
        });
    }
    /**
     * Handles item selections from the Navigation Drawer.
     */
    private boolean handleNavigationItemSelected(@NonNull MenuItem menuItem) {
        Class<? extends Fragment> fragmentClass = drawerFragmentMap.get(menuItem.getItemId());
        if (fragmentClass != null) {
            try {
                replaceFragment(fragmentClass.newInstance(), userEmail, userName, userImage);
                Log.d(TAG, "Navigated to: " + fragmentClass.getSimpleName());
            } catch (IllegalAccessException | InstantiationException e) {
                Log.e(TAG, "Error creating fragment instance", e);
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Close drawer after selection
            return true;
        }
        return false;
    }

    /**
     * Handles item selections from the Bottom Navigation View.
     */
    private boolean handleBottomNavigationItemSelected(@NonNull MenuItem item) {
        Class<? extends Fragment> fragmentClass = bottomNavFragmentMap.get(item.getItemId());
        if (fragmentClass != null) {
            try {
                replaceFragment(fragmentClass.newInstance(), userEmail, userName, userImage);
                Log.d(TAG, "Navigated to: " + fragmentClass.getSimpleName());
                return true;
            } catch (IllegalAccessException | InstantiationException e) {
                Log.e(TAG, "Error creating fragment instance", e);
            }
        } else if (item.getItemId() == R.id.menu) {
            drawerLayout.openDrawer(GravityCompat.START); // Open drawer
            return true;
        }
        return false;
    }

    /**
     * Replaces the current fragment with the specified one and adds the transaction to the back stack.
     */
    private void replaceFragment(Fragment fragment, String email, String name, String userImage) {
        Bundle args = new Bundle();
        args.putString("userEmail", email);
        args.putString("userName", name);
        args.putString("userImage", userImage);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
