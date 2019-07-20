package com.android.rr.potholes;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.rr.potholes.background.BackgroundLocationService;
import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.android.rr.potholes.presenters.MainActivityPresenter;

import java.util.HashMap;
import java.util.Map;

import static com.android.rr.potholes.potholesconstants.PotHolesConstants.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivity extends AppCompatActivity {

    private Intent mLocationServiceIntent;
    private MainActivityPresenter mMainActivityPresenter;
    private boolean isLocationEnabled;
    private boolean isPermissionEnabled;
    private AlertDialog mAlertDialog;
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainActivityPresenter = new MainActivityPresenter(MainActivity.this);
        mLocationServiceIntent = new Intent(MainActivity.this,
                BackgroundLocationService.class);

        //show error dialog if GoolglePlayServices not available
        if (!mMainActivityPresenter.isGooglePlayServicesAvailable()) {
            finishAffinity();
        }

        isPermissionEnabled = mMainActivityPresenter.checkAndRequestPermissions();

        Log.i(TAG, "isPermissionEnabled: "+isPermissionEnabled);

        findViewById(R.id.startServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionEnabled) {
                    isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
                    Log.i(TAG, "isLocationEnabled: "+isLocationEnabled);
                    if (!isLocationEnabled) {
                        mMainActivityPresenter.showDialogForGPSOn();
                    } else {
                        startLocationService();
                    }
                }
            }
        });

        findViewById(R.id.stopServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
            }
        });

        findViewById(R.id.writeToCSVFileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        mLocationServiceIntent.setAction(PotHolesConstants.ACTION_SHOW_FOREGROUND_NOTIFICATION);
        startForegroundService(mLocationServiceIntent);
    }*/

    /*@Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Location update onRestart .....................");

        isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
        if (!isLocationEnabled) {
             if (PotHolesConstants.isLocationServiceRunning)
                 stopLocalVoiceInteraction();
            mMainActivityPresenter.showDialogForGPSOn();
        } else {
            startLocationService();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        /*switch (requestCode) {
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
                            mMainActivityPresenter.showDialogForGPSOn();
                        } else {
                            startLocationService();
                        }
                    }
                } else {
                    mMainActivityPresenter.showToastAndFinish(
                            "Please grant location permission which is " +
                            "required for PotHoles app..");
                }
                return;
            }
        }*/
        if (requestCode == PotHolesConstants.MY_APP_PERMISSIONS_REQUEST_CODE) {
            HashMap<String, Integer> permissionsResults = new HashMap<>();
            int deniedCount = 0;

            for (int i=0; i<grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionsResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }
            Log.i(TAG, "onRequestPermissionsResult... deniedCount: "+
                    deniedCount);
            if (deniedCount == 0) {
                isLocationEnabled = mMainActivityPresenter.isLocationEnabled();
                Log.i(TAG, "onRequestPermissionsResult... isLocationEnabled: "+
                        isLocationEnabled);
                if (!isLocationEnabled) {
                    mMainActivityPresenter.showDialogForGPSOn();
                } else {
                    startLocationService();
                }
            } else {
                for (Map.Entry<String, Integer> entry : permissionsResults.entrySet()) {
                    String permissionName = entry.getKey();
//                    int permissionResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            MainActivity.this, permissionName)) {
                        showDialog(
                            "This app requires Location and Storage permissions to work without any problem.",
                            "Yes, Grant Permissions",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mMainActivityPresenter.checkAndRequestPermissions();
                                }
                            }, "No, Exit App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finishAffinity();
                                }
                            });
                    } else { // Permission is denied and never ask again is checked. shouldShowRequestPermissionRationale will return false.
                        showDialog("You have denied some permissions. Allow all permissions at " +
                                            "[Settings] > [Permissions]", "Goto Settings",
                             new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finishAffinity();
                                    }
                                }, "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finishAffinity();
                                    }
                                });
                            break;
                    }
                }
            }
        }
    }

    private void showDialog(String msg, String positiveLabel,
                            DialogInterface.OnClickListener positiveOnclick, String negativeLabel,
                            DialogInterface.OnClickListener negativeOnclick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(false);

        Log.e(TAG, "isActivityShown2: "+MainActivity.this.getWindow().getDecorView().getRootView().isShown());
        Log.e(TAG, "mAlertDialog: "+mAlertDialog);
        if (null == mAlertDialog) {
            mAlertDialog = builder.create();
            mAlertDialog.show();
        } else {
            mAlertDialog.dismiss();
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }

        //return  mAlertDialog;
    }

    private void startLocationService() {
//        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");

        /*ComponentName comp = new ComponentName(getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service;
        if (Build.VERSION.SDK_INT >= 26)
            service = startForegroundService(new Intent().setComponent(comp));
        else
            service = startService(new Intent().setComponent(comp));

        if (null == service){
            // something really wrong here
            Log.e(TAG, "Could not start service " + comp.toString());
        }*/

        if (!PotHolesConstants.isLocationServiceRunning) {
            mLocationServiceIntent.setAction(PotHolesConstants.ACTION_START_SERVICE);
            startForegroundService(mLocationServiceIntent);
            Toast.makeText(MainActivity.this, "Location updates is started.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService () {
        if (PotHolesConstants.isLocationServiceRunning) {
            mLocationServiceIntent.setAction(PotHolesConstants.ACTION_STOP_SERVICE);
            startForegroundService(mLocationServiceIntent);
            Toast.makeText(MainActivity.this, "Location updates is stopped.",
                    Toast.LENGTH_SHORT).show();
        }
    }

}