package com.android.rr.potholes;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.android.rr.potholes.presenters.MainActivityPresenter;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MainActivityPresenter mMainActivityPresenter;
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mMainActivityPresenter = new MainActivityPresenter(MainActivity.this);
        //show error dialog if GooglePlayServices not available
        if (!mMainActivityPresenter.isGooglePlayServicesAvailable()) {
            mMainActivityPresenter.showToast(getString(R.string.google_play_services_not_available));
            finishAffinity();
        }
        boolean isPermissionEnabled = mMainActivityPresenter.checkAndRequestPermissions();
        Log.i(TAG, "isPermissionEnabled: "+isPermissionEnabled);

        if (isPermissionEnabled)
            mMainActivityPresenter.checkGPSOnAndStartService();

        findViewById(R.id.startLocationServiceBtn).setOnClickListener(mMainActivityPresenter);
        findViewById(R.id.stopLocationServiceBtn).setOnClickListener(mMainActivityPresenter);
        findViewById(R.id.startAccelerationServiceBtn).setOnClickListener(mMainActivityPresenter);
        findViewById(R.id.stopAccelerationServiceBtn).setOnClickListener(mMainActivityPresenter);
        findViewById(R.id.writeToCSVFileBtn).setOnClickListener(mMainActivityPresenter);
        findViewById(R.id.shareFilesBtn).setOnClickListener(mMainActivityPresenter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PotHolesConstants.POTHOLES_PERMISSIONS_REQUEST_CODE) {
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
            if (deniedCount == 0)
                mMainActivityPresenter.checkGPSOnAndStartService();
            else {
                for (Map.Entry<String, Integer> entry : permissionsResults.entrySet()) {
                    String permissionName = entry.getKey();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            MainActivity.this, permissionName)) {
                        mMainActivityPresenter.showDialog(
                            getString(R.string.app_requiresPermissions),
                                getString(R.string.yes_grant),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mMainActivityPresenter.checkAndRequestPermissions();
                                }
                            }, getString(R.string.no_exit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finishAffinity();
                                }
                            });
                    } else { // Permission is denied and never ask again is checked.
                        // shouldShowRequestPermissionRationale will return false.
                        mMainActivityPresenter.showDialog(getString(R.string.you_have_denied),
                                getString(R.string.goto_settings),
                                new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mMainActivityPresenter.goToSettingsApp();
                                        finishAffinity();
                                    }
                                }, getString(R.string.no_exit), new DialogInterface.OnClickListener() {
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

}