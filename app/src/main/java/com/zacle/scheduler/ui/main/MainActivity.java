package com.zacle.scheduler.ui.main;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseActivity;
import com.zacle.scheduler.ui.chat.ChatFragment;

// TODO 1. create MainFragment to extends BaseFragment
// TODO 2. create application DI
// TODO 3. create room database
// TODO 4. create events repository
// TODO 5. create events model
// TODO 6. Fetch events from database
// TODO 7. RecyclerView to display events

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        }

        setUp();
    }

    @Override
    protected void setUp() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_schedule:
                        selectedFragment = new MainFragment();
                        break;
                    case R.id.nav_chats:
                        selectedFragment = new ChatFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return true;
            };

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }
}