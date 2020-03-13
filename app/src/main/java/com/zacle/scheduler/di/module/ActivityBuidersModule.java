package com.zacle.scheduler.di.module;

import com.zacle.scheduler.di.module.main.MainFragmentBuildersModule;
import com.zacle.scheduler.di.module.main.MainViewModelsModule;
import com.zacle.scheduler.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuidersModule {

    @ContributesAndroidInjector(modules = {MainFragmentBuildersModule.class, MainViewModelsModule.class})
    abstract MainActivity contributeMainActivity();
}
