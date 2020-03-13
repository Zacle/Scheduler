package com.zacle.scheduler.di.module;

import androidx.lifecycle.ViewModelProvider;

import com.zacle.scheduler.viewmodel.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelFactoryModule {

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory viewModelFactory);
}
