package com.android.rr.potholes.background;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * BackgroundLocationService used for tracking user location in the background.
 *
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Boolean servicesAvailable = false;
    private NotificationManager mNotificationManager;
    private final String TAG = BackgroundLocationService.class.getSimpleName();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(PotHolesConstants.LOCATION_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(PotHolesConstants.LOCAL_FASTEST_INTERVAL);

        servicesAvailable = servicesConnected();

        startForeground(PotHolesConstants.FOREGROUND_LOCATION_SERVICE_NOTIFICATION_ID,
                getNotification());

        setUpLocationClientIfNeeded();
    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        return ConnectionResult.SUCCESS == resultCode;
    }

    public int onStartCommand (@NonNull Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (null != intent.getAction() &&
                intent.getAction().equals(PotHolesConstants.ACTION_START_LOCATION_SERVICE)) {
            PotHolesConstants.isLocationServiceRunning = true;
            if (null != mNotificationManager)
                mNotificationManager.cancel(
                        PotHolesConstants.FOREGROUND_LOCATION_SERVICE_NOTIFICATION_ID);
        } else {
            if (null != intent.getAction() &&
                    intent.getAction().equals(PotHolesConstants.ACTION_STOP_LOCATION_SERVICE)) {
                PotHolesConstants.isLocationServiceRunning = false;
                stopForegroundService();

                return START_NOT_STICKY;
            }
        }

        setUpLocationClientIfNeeded();
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    private void setUpLocationClientIfNeeded() {
        if(mGoogleApiClient == null)
            buildGoogleApiClient();
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        String locationUpdate = android.text.format.DateFormat.format(
                "dd-MM-yyyy HH:mm:ss", new Date()).toString() + ", " +
                location.getLatitude() + ", " + location.getLongitude();
        Log.i("debug", locationUpdate);
        writeToFile( locationUpdate);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void writeToFile(String locationData) {
        File myDirectory = new File(Environment.getExternalStorageDirectory(),
                PotHolesConstants.POTHOLES_FOLDER_NAME);

        if (!myDirectory.exists()) {
            myDirectory.mkdir();
        }

        if (myDirectory.exists()) {
            File file = new File(myDirectory, PotHolesConstants.LOCATION_UPDATES_FILE_NAME);
            FileWriter writer;
            try {
                writer = new FileWriter(file, true);
                writer.append(locationData+"\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopForegroundService() {
        if (this.servicesAvailable && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;
        }

        if (null != mNotificationManager)
            mNotificationManager.cancel(
                    PotHolesConstants.FOREGROUND_LOCATION_SERVICE_NOTIFICATION_ID);
        stopForeground(true);
        stopSelf();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "onConnected.... is called..");
        // Request location updates using static settings
        //Intent intent = new Intent(this, LocationReceiver.class);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this); // This is the changed line.
//        PendingIntent pendingIntent = PendingIntent
//                .getBroadcast(this, 54321, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
//                mLocationRequest, pendingIntent);

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended.... is called..");
        // Destroy the current location client
        mGoogleApiClient = null;
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed.... is called..");
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel(
                "channel_01",
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
        );

        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01");
        //builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);

        return builder.build();
    }
}