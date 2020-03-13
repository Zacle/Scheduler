package com.zacle.scheduler.di.module.main;

import androidx.lifecycle.ViewModel;

import com.zacle.scheduler.di.annotation.ViewModelKey;
import com.zacle.scheduler.ui.chat.ChatViewModel;
import com.zacle.scheduler.ui.main.MainViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class MainViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    public abstract ViewModel bindMainViewModel(MainViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel.class)
    public abstract ViewModel bindChatViewModel(ChatViewModel viewModel);
}
