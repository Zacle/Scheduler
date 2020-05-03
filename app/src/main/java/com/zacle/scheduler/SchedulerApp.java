package com.zacle.scheduler;


import android.content.Context;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.zacle.scheduler.di.component.DaggerApplicationComponent;
import com.zacle.scheduler.ui.chat.adapter.MyAppInterfaceAdapter;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class SchedulerApp extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        try {
            // Create a new configuration
            Configuration.Builder builder = new Configuration.Builder();

            // Perform any other configuration steps (optional)
            builder.firebaseRootPath("prod");

            // Enable location messages
            builder.googleMaps(getString(R.string.google_maps_key));

            // Rooms creations
            builder.publicRoomCreationEnabled(true);

            // Initialize the Chat SDK
            ChatSDK.initialize(context, builder.build(), FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);

            // File storage is needed for profile image upload and image messages
            FirebaseFileStorageModule.activate();

            // Push notification module
            FirebasePushModule.activate();

            // Set Chat Main Activity
            ChatSDK.shared().setInterfaceAdapter(new MyAppInterfaceAdapter(this));

            // Activate any other modules you need.
            // ...

        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        }

        // Uncomment this to enable Firebase UI
        FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationComponent.builder().application(this).build();
    }
}
