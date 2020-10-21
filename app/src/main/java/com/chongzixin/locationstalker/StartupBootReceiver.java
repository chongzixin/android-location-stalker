package com.chongzixin.locationstalker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StartupBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("StartupBootReceiver", "boot completed from com.google.locationupdatesforegroundservice");

        // TODO: change this to start alarm
    }
}
