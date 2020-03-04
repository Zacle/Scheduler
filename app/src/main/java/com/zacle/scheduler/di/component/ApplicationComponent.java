package com.zacle.scheduler.di.component;


import android.app.Application;
import android.content.Context;

import com.zacle.scheduler.SchedulerApp;
import com.zacle.scheduler.di.ApplicationContext;
import com.zacle.scheduler.di.DatabaseInfo;
import com.zacle.scheduler.di.module.ApplicationModule;
import com.zacle.scheduler.di.module.DatabaseModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        DatabaseModule.class
})
public interface ApplicationComponent {
    void inject(SchedulerApp schedulerApp);

    @ApplicationContext
    Context getContext();

    Application getApplication();

    @DatabaseInfo
    String getDatabaseName();
}
