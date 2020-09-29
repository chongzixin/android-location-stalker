/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdatesforegroundservice;


import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    static String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS");
        Date now = new Date();
        return df.format(now);
    }

    static String getLocationStringToPersist(Location location) {
        return getCurrentDateTime() + " (" + location.getExtras().getString(AlarmReceiver.LOCATION_EXTRAS)+ "): " + getLocationText(location);
    }

    static void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("location.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data + "\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("location.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                List<String> tmp = new ArrayList<String>();

                StringBuilder stringBuilder = new StringBuilder();
                // read the file in reverse order and put into arraylist.
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    tmp.add(receiveString);
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();

                // only display in reverse order if it's above Android 8.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Collections.reverse(tmp);
                    ret = String.join("\n", tmp);
                } else {
                    ret = stringBuilder.toString();
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.e("location activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("location activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}

