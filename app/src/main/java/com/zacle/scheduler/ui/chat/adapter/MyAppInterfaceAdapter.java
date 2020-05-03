package com.zacle.scheduler.ui.chat.adapter;


import android.content.Context;

import com.zacle.scheduler.ui.chat.main.ChatMainActivity;

import co.chatsdk.ui.manager.BaseInterfaceAdapter;

public class MyAppInterfaceAdapter extends BaseInterfaceAdapter {

    public MyAppInterfaceAdapter(Context context) {
        super(context);
    }

    public Class getMainActivity() {
        return ChatMainActivity.class;
    }

}
