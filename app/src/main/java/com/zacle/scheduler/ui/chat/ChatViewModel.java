package com.zacle.scheduler.ui.chat;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

public class ChatViewModel extends ViewModel {

    private static final String TAG = "ChatViewModel";
    
    @Inject
    public ChatViewModel() {
        Log.d(TAG, "ChatViewModel: chatviewmodel is ready...");
    }
}
