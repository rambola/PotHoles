package com.android.rr.potholes.presenters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.android.rr.potholes.MainActivity;

import static com.android.rr.potholes.potholesconstants.PotHolesConstants.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivityPresenter {
    private MainActivity mMainActivity;

    public MainActivityPresenter (MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    public boolean checkForPermissions () {
        if (ContextCompat.checkSelfPermission(mMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mMainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mMainActivity).setTitle("Location Permission")
                        .setMessage("PotHoles requires Location Permission. Please allow permission.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(mMainActivity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create().show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(mMainActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean isLocationEnabled () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) mMainActivity.getSystemService(
                    Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(mMainActivity.getContentResolver(),
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    public void showDialogForOnLocation () {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setTitle("Enable Location")
                .setMessage("Go to Settings")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mMainActivity.startActivity(new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                showToastAndFinish("Please enable GPS..");
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showToastAndFinish (String toastMsg) {
        Toast.makeText(mMainActivity, toastMsg, Toast.LENGTH_SHORT).show();

        mMainActivity.finishAffinity();
    }
}