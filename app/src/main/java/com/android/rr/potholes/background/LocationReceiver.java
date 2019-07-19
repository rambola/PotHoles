package com.android.rr.potholes.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class LocationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;


    @Override
    public void onReceive(Context context, Intent intent) {

//        Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
        // Need to check and grab the Intent's extras like so
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
        }
    }
}