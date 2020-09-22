package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_SERVICE_ID = 101;
    private static final String TAG = "ALARM_RECEIVER";
    static final String INTENT_EXTRA = "MESSAGE";
    static final String ACTION_BROADCAST = Utils.PACKAGE_NAME + ".broadcast";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(final Context context, Intent intent) {
        // schedule for next alarm
        scheduleExactAlarm(context, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FOREGROUNDAPP_ALARMRECEIVER_WAKELOCK:ALARM_RECEIVER");
        wakeLock.acquire();

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                // do job
                String datetime = Utils.getCurrentDateTime();
                Log.i(TAG, datetime + " in AlarmManager run()");

                writeDateTimeToFile(context);

                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST);
                intent.putExtra(INTENT_EXTRA, datetime);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                // TODO: FIX THIS SO THAT NOTIFICATION GETS UPDATED.
                // update the notification
                // context.getSystemService(Context.NOTIFICATION_SERVICE).notify(NOTIFICATION_SERVICE_ID, getNotification());
            }
        };

        handler.post(periodicUpdate);
        wakeLock.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarms.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+60*1000, null), pendingIntent);
//        alarms.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+60*1000-SystemClock.elapsedRealtime()%1000, pendingIntent);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarms.cancel(pendingIntent);
    }

    private void writeDateTimeToFile(Context context) {
        String currentTime = Utils.getCurrentDateTime();
        Utils.writeToFile(currentTime, context);
    }
}
