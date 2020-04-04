package com.zacle.scheduler.di.module;

import com.zacle.scheduler.di.module.addOrEdit.AddEditFragmentBuildersModule;
import com.zacle.scheduler.di.module.main.MainFragmentBuildersModule;
import com.zacle.scheduler.di.module.main.MainViewModelsModule;
import com.zacle.scheduler.ui.addOrEdit.AddEditActivity;
import com.zacle.scheduler.ui.main.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuidersModule {

    @ContributesAndroidInjector(modules = {MainFragmentBuildersModule.class, MainViewModelsModule.class})
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = {AddEditFragmentBuildersModule.class})
    abstract AddEditActivity contibuteAddEditActivity();
}
