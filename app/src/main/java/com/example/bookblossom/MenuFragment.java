package com.example.bookblossom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;

public class MenuFragment extends Fragment {

    private static final String TAG = "MenuFragment";
    private DrawerLayout drawerLayout;
    private HashMap<Integer, Fragment> fragmentMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        NavigationView navigationView = view.findViewById(R.id.nav_view);

        // Initialize the fragment map
        initializeFragmentMap();

        // Setup Navigation Drawer
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Fragment selectedFragment = fragmentMap.get(menuItem.getItemId());
            if (selectedFragment != null) {
                Log.d(TAG, "Fragment selected: " + selectedFragment.getClass().getSimpleName());
                replaceFragment(selectedFragment);
            } else {
                Log.d(TAG, "No fragment found for menu item ID: " + menuItem.getItemId());
            }
            // Close the drawer when item is selected
            drawerLayout.closeDrawers();
            return true;
        });


        // Set OnClickListener on the button to open the drawer
        navigationView.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));


        return view;
    }

    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.nav_authors, new AuthorsFragment());
        // fragmentMap.put(R.id.nav_formats, new FormatsFragment());
        fragmentMap.put(R.id.nav_settings, new SettingsFragment());
        fragmentMap.put(R.id.nav_app_info, new AppInfoFragment());
        fragmentMap.put(R.id.nav_send_feedback, new FeedbackFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
