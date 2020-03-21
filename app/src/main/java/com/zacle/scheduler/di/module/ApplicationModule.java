package com.zacle.scheduler.di.module;

import com.zacle.scheduler.utils.AppExecutors;


import dagger.Module;
import dagger.Provides;

/**
 * Application module for dependency injection
 */

@Module
public class ApplicationModule {

    @Provides
    static AppExecutors provideAppExecutors() {
        return new AppExecutors();
    }
}
