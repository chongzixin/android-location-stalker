package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.app.Application;
import android.content.Context;

public class LocationStalkerApp extends Application {
    private static LocationStalkerApp instance;

    public static LocationStalkerApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
