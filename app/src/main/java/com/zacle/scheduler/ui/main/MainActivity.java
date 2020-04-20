package com.zacle.scheduler.ui.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zacle.scheduler.R;
import com.zacle.scheduler.ui.base.BaseActivity;
import com.zacle.scheduler.ui.chat.ChatFragment;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    public static final String ID = "com.zacle.scheduler.ui.main.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        }

        Intent intent = getIntent();
        int notificationId = intent.getIntExtra(ID, -1);
        if (notificationId != -1) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
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