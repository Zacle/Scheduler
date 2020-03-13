package com.zacle.scheduler.di.module.main;

import com.zacle.scheduler.ui.chat.ChatFragment;
import com.zacle.scheduler.ui.main.MainFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    abstract ChatFragment contributeChatFragment();
}
