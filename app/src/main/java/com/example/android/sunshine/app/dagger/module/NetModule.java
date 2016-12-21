package com.example.android.sunshine.app.dagger.module;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rkrde on 15-12-2016.
 */
@Module
public class NetModule {

    Context context;
    public NetModule(Context context) {
        this.context = context;
    }


    @Provides
    @Singleton
    GoogleApiClient provideGogleApiClient(Application application) {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(application)
                .addApiIfAvailable(Wearable.API)
//                .addConnectionCallbacks(application)
//                .addOnConnectionFailedListener(application)
                .build();
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }
}
