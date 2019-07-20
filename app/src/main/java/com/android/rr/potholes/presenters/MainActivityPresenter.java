package com.android.rr.potholes.presenters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.rr.potholes.MainActivity;
import com.android.rr.potholes.R;
import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import static com.android.rr.potholes.potholesconstants.PotHolesConstants.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivityPresenter {
    private MainActivity mMainActivity;
    private final String TAG = MainActivityPresenter.class.getSimpleName();
    private String[] mAppPermissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    public MainActivityPresenter (MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    /*public boolean checkAndRequestPermissions() {
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
    }*/

    public boolean checkAndRequestPermissions() {
        List<String> listOfPermissionsNeeded = new ArrayList<>();
        for (String appPermission : mAppPermissions) {
            if (ContextCompat.checkSelfPermission(mMainActivity, appPermission)
                    != PackageManager.PERMISSION_GRANTED)
                listOfPermissionsNeeded.add(appPermission);
        }

        if (!listOfPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mMainActivity,
                    listOfPermissionsNeeded.toArray(new String[listOfPermissionsNeeded.size()]),
                    PotHolesConstants.MY_APP_PERMISSIONS_REQUEST_CODE);
            Log.e(TAG, "requestPermissions...........");
            return false;
        }

        return  true;
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

    public void showDialogForGPSOn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setTitle(R.string.app_name)
                .setMessage("Please Enable Location/GPS")
                .setPositiveButton("Yes, Goto Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                mMainActivity.startActivity(new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("No, Exit app",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showToastAndFinish("Please enable GPS/Location and mobile data to get accurate location updates...");
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showToastAndFinish (String toastMsg) {
        Toast.makeText(mMainActivity, toastMsg, Toast.LENGTH_SHORT).show();
        mMainActivity.finishAffinity();
    }

    public boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().
                isGooglePlayServicesAvailable(mMainActivity);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(mMainActivity, status, 0).show();
            return false;
        }
    }
}