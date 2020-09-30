package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_SERVICE_ID = 101;
    private static final int ALARM_FREQUENCY = 60*1000;
    private static final String TAG = "ALARM_RECEIVER";
    static final String ACTION_BROADCAST = Utils.PACKAGE_NAME + ".broadcast";

    static final String LOCATION_EXTRAS = "SOURCE";
    static final String EXTRA_FROM_ALARM_RECEIVER = "alarmreceiver";

    static final int LOCATION_INTERVAL = 10*1000;
    static final int FASTEST_LOCATION_INTERVAL = LOCATION_INTERVAL/2;

    static Location location;
    static FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationStalkerApp.getContext());
    static LocationRequest locationRequest = LocationRequest.create();
    static LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            location = locationResult.getLastLocation();
            Log.i(TAG, "Got a new location: " + Utils.getLocationText(location));
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(final Context context, Intent intent) {
        // schedule for next alarm
        scheduleExactAlarm(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_ALARMRECEIVER_WAKELOCK:ALARM_RECEIVER");
        wakeLock.acquire();

        // process the current location if it exists, particularly important because location may not be available when the app is first launched or if permission is not given
        if(location != null)
            processLocation(location);

        // prepare to request for location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);

        try {
            Log.d(TAG, "requesting location updates");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(context, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }

//
//        Handler handler = new Handler();
//        Runnable periodicUpdate = new Runnable() {
//            @Override
//            public void run() {
//                // do job
//                final Context context = LocationStalkerApp.getContext();
//
//                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                locationRequest.setInterval(LOCATION_INTERVAL);
//                locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
//
//                try {
//                    Log.d(TAG, "requesting location updates");
//                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
//                }
//                catch (SecurityException unlikely) {
//                    Utils.setRequestingLocationUpdates(context, false);
//                    Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
//                }
//                catch (Exception e) {
//                    Log.e(TAG, "Error here: " + e);
//                }
//            }
//        };
//
//        handler.post(periodicUpdate);

        // TODO: consider removing locationupdates in future
        // Log.d(TAG, "removing location updates");
        // fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        wakeLock.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        // TODO: remove when done debugging
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS");
        Date resultdate = new Date((System.currentTimeMillis()+ALARM_FREQUENCY));
        String dateToShow = sdf.format(resultdate);
        Log.d(TAG, "scheduling next alarm at " + dateToShow);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarms.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ALARM_FREQUENCY, null), pendingIntent);
    }

    // TODO: cancel alarm when updates are no longer required. 
    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarms.cancel(pendingIntent);
    }

    private static void processLocation(Location location) {
        Context context = LocationStalkerApp.getContext();

        Bundle bundle = new Bundle();
        bundle.putString(LOCATION_EXTRAS, EXTRA_FROM_ALARM_RECEIVER);

        location.setExtras(bundle);

        Log.i(TAG, "can get location here leh: " + Utils.getLocationText(location));
        String toWrite = Utils.getLocationStringToPersist(location);
        Utils.writeToFile(toWrite, context);

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(LocationUpdatesService.EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        // TODO: FIX THIS SO THAT NOTIFICATION GETS UPDATED.
        // update the notification
        // context.getSystemService(Context.NOTIFICATION_SERVICE).notify(NOTIFICATION_SERVICE_ID, getNotification());
    }
}
