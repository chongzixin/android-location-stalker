package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_SERVICE_ID = 101;
    private static final int ALARM_FREQUENCY = 60 * 1000;
    private static final String TAG = "ALARM_RECEIVER";
    static final String ACTION_BROADCAST = Utils.PACKAGE_NAME + ".broadcast";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(final Context context, Intent intent) {
        // schedule for next alarm
        scheduleExactAlarm(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_ALARMRECEIVER_WAKELOCK:ALARM_RECEIVER");
        wakeLock.acquire();

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                // do job
                final Context context = LocationStalkerApp.getContext();
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // assume the user already has permissions so we do nothing here.
                    Log.i(TAG, "we are here");
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(LocationUpdatesService.LOCATION_EXTRAS, LocationUpdatesService.EXTRA_FROM_ALARM_RECEIVER);

                                    Location location = task.getResult();
                                    location.setExtras(bundle);

                                    Log.i(TAG, "can get location here leh: " + location);
                                    String toWrite = Utils.getLocationStringToPersist(location);
                                    Utils.writeToFile(toWrite, context);

                                    // Notify anyone listening for broadcasts about the new location.
                                    Intent intent = new Intent(ACTION_BROADCAST);
                                    intent.putExtra(LocationUpdatesService.EXTRA_LOCATION, location);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                    // TODO: FIX THIS SO THAT NOTIFICATION GETS UPDATED.
                                    // update the notification
                                    // context.getSystemService(Context.NOTIFICATION_SERVICE).notify(NOTIFICATION_SERVICE_ID, getNotification());
                                } else {
                                    Log.w(TAG, "Failed to get location.");
                                }
                            }
                        }); }
        };

        handler.post(periodicUpdate);
        wakeLock.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarms.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ALARM_FREQUENCY, null), pendingIntent);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarms.cancel(pendingIntent);
    }
}
