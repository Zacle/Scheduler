package com.zacle.scheduler.di.module.addOrEdit;

import com.zacle.scheduler.ui.addOrEdit.AddEditFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AddEditFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract AddEditFragment contibuteAddEditFragment();
}
