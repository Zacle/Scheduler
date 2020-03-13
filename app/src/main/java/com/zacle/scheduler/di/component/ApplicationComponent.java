package com.zacle.scheduler.di.component;


import android.app.Application;

import com.zacle.scheduler.SchedulerApp;
import com.zacle.scheduler.di.annotation.DatabaseInfo;
import com.zacle.scheduler.di.module.ActivityBuidersModule;
import com.zacle.scheduler.di.module.ApplicationModule;
import com.zacle.scheduler.di.module.DatabaseModule;
import com.zacle.scheduler.di.module.ViewModelFactoryModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ActivityBuidersModule.class,
        ApplicationModule.class,
        DatabaseModule.class,
        ViewModelFactoryModule.class
})
public interface ApplicationComponent extends AndroidInjector<SchedulerApp> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        ApplicationComponent build();
    }

    @DatabaseInfo
    String getInfo();
}
