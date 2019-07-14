package com.android.rr.potholes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.rr.potholes.pothoelsinterfaces.IMainActivityInterface;
import com.android.rr.potholes.presenters.MainActivityPresenter;

import static com.android.rr.potholes.potholesconstants.PotHolesConstants.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivity extends AppCompatActivity implements IMainActivityInterface {
    private MainActivityPresenter mMainActivityPresenter;
    boolean isLocationEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainActivityPresenter = new MainActivityPresenter(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mMainActivityPresenter.checkForPermissions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
        if (!isLocationEnabled) {
            mMainActivityPresenter.showDialogForOnLocation();
        } else {
            gpsIsOn();
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
                        if (!isLocationEnabled) {
                            mMainActivityPresenter.showDialogForOnLocation();
                        } else {
                            gpsIsOn();
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

    @Override
    public void gpsIsOn() {

    }
}