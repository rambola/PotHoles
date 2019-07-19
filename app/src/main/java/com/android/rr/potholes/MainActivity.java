package com.android.rr.potholes;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.rr.potholes.background.BackgroundLocationService;
import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.android.rr.potholes.presenters.MainActivityPresenter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import static com.android.rr.potholes.potholesconstants.PotHolesConstants.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MainActivityPresenter mMainActivityPresenter;
    private boolean isLocationEnabled, isPermissionEnabled;
    private final String TAG = MainActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mMainActivityPresenter = new MainActivityPresenter(MainActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPermissionEnabled = mMainActivityPresenter.checkForPermissions();
        }

        Log.i(TAG, "isPermissionEnabled: "+isPermissionEnabled);

        if (isPermissionEnabled) {
            isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
            Log.i(TAG, "isLocationEnabled: "+isLocationEnabled);
            if (!isLocationEnabled) {
                mMainActivityPresenter.showDialogForOnLocation();
            } else {
                if (mGoogleApiClient.isConnected())
                    startLocationUpdates();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Location update onRestart .....................");

        isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
        if (!isLocationEnabled) {
            mMainActivityPresenter.showDialogForOnLocation();
        } else {
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
                        Log.i(TAG, "onRequestPermissionsResult... isLocationEnabled: "+
                                isLocationEnabled);
                        if (!isLocationEnabled) {
                            mMainActivityPresenter.showDialogForOnLocation();
                        } else {
                            startLocationUpdates();
                        }
                    }
                } else {
                    mMainActivityPresenter.showToastAndFinish(
                            "Please grant location permission which is " +
                            "required for PotHoles app..");
                }
                return;
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    protected void startLocationUpdates() {
//        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");

        ComponentName comp = new ComponentName(getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service;
        if (Build.VERSION.SDK_INT >= 26)
            service = startForegroundService(new Intent().setComponent(comp));
        else
            service = startService(new Intent().setComponent(comp));

        if (null == service){
            // something really wrong here
            Log.e(TAG, "Could not start service " + comp.toString());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy().....");
        if (null != mFusedLocationClient && null != mLocationCallback) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        if (null != mGoogleApiClient) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(PotHolesConstants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(PotHolesConstants.LOCAL_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!isLocationEnabled) {
            mMainActivityPresenter.showDialogForOnLocation();
        } else {
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mLocation = location;
        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        Log.i(TAG, "onLocationChanged... mLastUpdateTime: "+mLastUpdateTime);
        Log.i(TAG, "onLocationChanged... latitude: "+mLocation.getLatitude());
        Log.i(TAG, "onLocationChanged... longitude: "+mLocation.getLongitude());
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }
}