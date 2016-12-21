package com.example.android.sunshine.app.dagger.component;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.services.NotifyWearService;
import com.example.android.sunshine.app.dagger.module.AppModule;
import com.example.android.sunshine.app.dagger.module.NetModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by rkrde on 15-12-2016.
 */

@Singleton
@Component(modules = {NetModule.class, AppModule.class})
public interface NetComponent {
    void inject(NotifyWearService service);
    void inject(MainActivity activity);

}
