package com.zacle.scheduler.ui.chat.threads;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zacle.scheduler.R;
import com.zacle.scheduler.data.model.UserLocation;
import com.zacle.scheduler.ui.chat.contacts.ContactsFragment;
import com.zacle.scheduler.ui.chat.locations.ChatUserLocationsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.threads.ThreadDetailsActivity;
import timber.log.Timber;

import static com.zacle.scheduler.utils.AppConstants.MAPVIEW_BUNDLE_KEY;

public class MyThreadDetailsActivity extends ThreadDetailsActivity {

    private static final String TAG = "MyThreadDetailsActivity";

    @BindView(R.id.thread_detail_image)
    public ImageView image;
    @BindView(R.id.thread_detail_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.thread_detail_collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbarLayout;

    protected ContactsFragment contactsFragment;

    protected ActionBar actionBar;
    protected MenuItem settingsItem;

    private FirebaseFirestore db;
    private List<User> users;
    private List<UserLocation> userLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
        } catch (NullPointerException ex) {
            Log.e(TAG, "onCreate: NullPointerException occurred but ignored: " + ex.getMessage());
        }

        setContentView(R.layout.activity_my_thread_details);

        db = FirebaseFirestore.getInstance();

        getChatRoomUsers();

        ButterKnife.bind(this);

        initViews();

    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.threadDetailsUpdated())
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> reloadData()));

        reloadData();
    }

    @Override
    protected void reloadData() {
        actionBar = getSupportActionBar();
        String name = Strings.nameForThread(thread);
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(name);

        if (!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
//            image.setOnClickListener(v -> zoomImageFromThumbnail(image, thread.getImageUrl()));
            Glide.with(this)
                    .load(thread.getImageUrl())
                    .placeholder(R.drawable.profile_default)
                    .centerCrop()
                    .into(image);
        } else {
            image.setOnClickListener(null);
        }

        // CoreThread users bundle
        if (contactsFragment == null) {
            contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), thread.getName());
            contactsFragment.setExtraData(thread.getEntityID());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_thread_details, contactsFragment).commit();
        } else {
            contactsFragment.loadData(false);
        }
    }

    private void getChatRoomUsers() {
        users = thread.getUsers();

        userLocations = new ArrayList<>();

        for(User user: users) {
            getUserLocation(user);
        }
    }

    private void getUserLocation(final User user) {
        DocumentReference locationsRef = db
                .collection(getString(R.string.users_location_collection))
                .document(user.getEntityID());

        locationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserLocation userLocation = task.getResult().toObject(UserLocation.class);
                if (userLocation != null) {
                    Timber.d("getUserLocation: user = %s", userLocation.getUser().getUsername());
                    Timber.d("getUserLocation: userName = %s and userID = %s", user.getName(), user.getEntityID());
                    Timber.d("getUserLocation: location = %s", userLocation.getGeoPoint());
                    userLocations.add(userLocation);
                }
            }
        });
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
        } catch (NullPointerException ex) {
            Log.e(TAG, "onResume: NullPointerException occurred but ignored: " + ex.getMessage());
        }
        reloadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threads_menu, menu);

        // Only the creator can modify the group. Also, private 1-to-1 chats can't be edited
        if (!thread.getCreatorEntityId().equals(ChatSDK.currentUserID()) || thread.typeIs(ThreadType.Private1to1)) {
            menu.removeItem(R.id.action_edit);
        }
        // Only private chats users can be able to see each other locations
        if (thread.typeIs(ThreadType.Public) || thread.typeIs(ThreadType.PublicGroup)) {
            menu.removeItem(R.id.current_location);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_edit) {
            ChatSDK.ui().startThreadEditDetailsActivity(ChatSDK.shared().context(), thread.getEntityID());
        }
        if (item.getItemId() == R.id.action_mute) {
            if (thread.metaValueForKey(Keys.Mute) != null) {
                ChatSDK.thread().unmuteThread(thread).subscribe(new CrashReportingCompletableObserver());
            } else {
                ChatSDK.thread().muteThread(thread).subscribe(new CrashReportingCompletableObserver());
            }
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.current_location) {
            Intent intent = ChatUserLocationsActivity.newIntent(this, thread.getEntityID());
            Bundle args = new Bundle();
            args.putParcelableArrayList(ChatUserLocationsActivity.USER_LOCATIONS_BUNDLE, (ArrayList<? extends Parcelable>) userLocations);
            intent.putExtra(ChatUserLocationsActivity.USER_LOCATIONS, args);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_mute);

        String muteText = getApplicationContext().getString(R.string.mute_notifications);
        String unmuteText = getApplicationContext().getString(R.string.unmute_notifications);

        if (thread.metaValueForKey(Keys.Mute) != null) {
            item.setTitle(unmuteText);
        } else {
            item.setTitle(muteText);
        }

        return super.onPrepareOptionsMenu(menu);
    }

}
