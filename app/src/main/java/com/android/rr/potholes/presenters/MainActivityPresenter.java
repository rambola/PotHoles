package com.android.rr.potholes.presenters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.rr.potholes.MainActivity;
import com.android.rr.potholes.R;
import com.android.rr.potholes.background.BackgroundAcceleratorService;
import com.android.rr.potholes.background.BackgroundLocationService;
import com.android.rr.potholes.background.ExportDataToCSVTask;
import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivityPresenter implements View.OnClickListener {
    private MainActivity mMainActivity;
    private Intent mLocationServiceIntent;
    private Intent mAccelerometerServiceIntent;
    private androidx.appcompat.app.AlertDialog mAlertDialog;
    private ExportDataToCSVTask mExportDataToCSVTask;

    private final String TAG = MainActivityPresenter.class.getSimpleName();
    private String[] mAppPermissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    public MainActivityPresenter (MainActivity mainActivity) {
        mMainActivity = mainActivity;
        mLocationServiceIntent = new Intent(mainActivity,
                BackgroundLocationService.class);
        mAccelerometerServiceIntent = new Intent(mainActivity,
                BackgroundAcceleratorService.class);
    }

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
                    PotHolesConstants.POTHOLES_PERMISSIONS_REQUEST_CODE);
            Log.e(TAG, "checkAndRequestPermissions...........");
            return false;
        }

        return  true;
    }

    private boolean isLocationEnabled () {
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

    private void showDialogForGPSOn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setTitle(R.string.app_name)
                .setMessage(mMainActivity.getString(R.string.enable_location_gps))
                .setPositiveButton(mMainActivity.getString(R.string.yes_goto_settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                mMainActivity.startActivity(new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton(mMainActivity.getString(R.string.no_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showToast(mMainActivity.getString(
                                        R.string.please_enable_location_gps));
                                mMainActivity.finishAffinity();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showToast(String toastMsg) {
        Toast.makeText(mMainActivity, toastMsg, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startLocationServiceBtn:
                boolean isPermissionEnabled = checkAndRequestPermissions();
                Log.i(TAG, "isPermissionEnabled: "+isPermissionEnabled);
                if (isPermissionEnabled) {
                    checkGPSOnAndStartService();
                }
                break;
            case R.id.stopLocationServiceBtn:
                startOrStopLocationService(PotHolesConstants.ACTION_STOP_LOCATION_SERVICE);
                break;
            case R.id.startAccelerationServiceBtn:
                startOrStopAccelerometerService(PotHolesConstants.ACTION_START_ACCELEROMETER_SENSOR);
                break;
            case R.id.stopAccelerationServiceBtn:
                startOrStopAccelerometerService(PotHolesConstants.ACTION_STOP_ACCELEROMETER_SENSOR);
                break;
            case R.id.writeToCSVFileBtn:
                writeToCSVFile();
                break;
            case R.id.shareFilesBtn:
                shareFiles();
                break;
        }
    }

    public void checkGPSOnAndStartService () {
        boolean isLocationEnabled = isLocationEnabled();
        Log.i(TAG, "checkGPSOnAndStartService... isLocationEnabled: "+isLocationEnabled);
        if (isLocationEnabled)
            checkForNetworkConnectionAndStartService();
        else
            showDialogForGPSOn();
    }

    private void checkForNetworkConnectionAndStartService () {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mMainActivity.getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean  dataConnectionAvailable = (null != activeNetwork && activeNetwork.isConnected());
        Log.e(TAG, "checkForNetworkConnectionAndStartService... dataConnectionAvailable: "+
                dataConnectionAvailable);
        if (dataConnectionAvailable)
            startOrStopLocationService(PotHolesConstants.ACTION_START_LOCATION_SERVICE);
        else
            showDialogForNetworkConnection();
    }

    private void showDialogForNetworkConnection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setTitle(R.string.app_name)
                .setMessage(mMainActivity.getString(R.string.no_internet))
                .setPositiveButton(mMainActivity.getString(R.string.yes_goto_settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);//android.provider.Settings.ACTION_SETTINGS //Intent.ACTION_MAIN
//                                intent.setClassName("com.qualcomm.qti.networksetting",
//                                        "com.qualcomm.qti.networksetting.MobileNetworkSettings");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                mMainActivity.startActivity(intent);
                                mMainActivity.startActivity(new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS));
//                                mMainActivity.startActivity(new Intent(
//                                        Settings.ACTION_WIFI_SETTINGS));
                            }
                        })
                .setNegativeButton(mMainActivity.getString(R.string.no_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                mMainActivity.finishAffinity();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startOrStopLocationService (String action) {
        Log.e(TAG, "startOrStopLocationService... action: "+action+
                ", isLocationServiceRunning: "+PotHolesConstants.isLocationServiceRunning);
        if (!PotHolesConstants.isLocationServiceRunning &&
                action.equals(PotHolesConstants.ACTION_START_LOCATION_SERVICE)) {
            mLocationServiceIntent.setAction(action);
            showToast("Location updates is started.");
        } else {
            mLocationServiceIntent.setAction(action);
            showToast("Location updates is stopped.");
        }
        mMainActivity.startForegroundService(mLocationServiceIntent);
    }

    private void startOrStopAccelerometerService(String action) {
        Log.e(TAG, "startOrStopAccelerometerService... action: "+action+
                ", isAccelerometerServiceRunning: "+PotHolesConstants.isAccelerometerServiceRunning);
        if (!PotHolesConstants.isAccelerometerServiceRunning &&
                action.equals(PotHolesConstants.ACTION_START_ACCELEROMETER_SENSOR)) {
            mAccelerometerServiceIntent.setAction(action);
            showToast("Accelerometer sensor updates is started.");
        } else {
            mAccelerometerServiceIntent.setAction(action);
            showToast("Accelerometer sensor updates is stopped.");
        }
        mMainActivity.startForegroundService(mAccelerometerServiceIntent);
    }

    public void goToSettingsApp () {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", mMainActivity.getPackageName(),
                        null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mMainActivity.startActivity(intent);
    }

    public void showDialog(String msg, String positiveLabel,
                            DialogInterface.OnClickListener positiveOnclick, String negativeLabel,
                            DialogInterface.OnClickListener negativeOnclick) {
        androidx.appcompat.app.AlertDialog.Builder builder = new
                androidx.appcompat.app.AlertDialog.Builder(mMainActivity);
        builder.setTitle(R.string.app_name);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(false);

        Log.e(TAG, "isActivityShown2: "+mMainActivity.getWindow().getDecorView().getRootView().isShown());
        Log.e(TAG, "mAlertDialog: "+mAlertDialog);
        if (null == mAlertDialog) {
            mAlertDialog = builder.create();
            mAlertDialog.show();
        } else {
            mAlertDialog.dismiss();
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    private void writeToCSVFile () {
        if (null != mExportDataToCSVTask && (mExportDataToCSVTask.getStatus() ==
                AsyncTask.Status.PENDING || mExportDataToCSVTask.getStatus() ==
                AsyncTask.Status.RUNNING)) {
            mExportDataToCSVTask.cancel(true);
            mExportDataToCSVTask = null;
        }
        mExportDataToCSVTask = new ExportDataToCSVTask(mMainActivity,
                MainActivityPresenter.this);
        mExportDataToCSVTask.execute();
    }

    private void shareFiles () {
        File exportDir = new File(Environment.getExternalStorageDirectory(),
                PotHolesConstants.POTHOLES_FOLDER_NAME);
        if (exportDir.exists()) {
            ArrayList<Uri> files = new ArrayList<>();
            File locationCsvFile = new File(exportDir,
                    PotHolesConstants.LOCATION_UPDATES_CSV_FILE_NAME);
            File accelerometerCsvFile = new File(exportDir,
                    PotHolesConstants.ACCELEROMETER_UPDATES_CSV_FILE_NAME);

            boolean locationCsvExists = locationCsvFile.exists();
            boolean accelerometerCsvExists = accelerometerCsvFile.exists();

            Log.i(TAG, "shareFiles... locationCsvExists: "+locationCsvExists+
                    ", accelerometerCsvExists: "+accelerometerCsvExists);

            Intent shareIntent;
            if (locationCsvExists && accelerometerCsvExists) {
                shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                Uri uri = Uri.fromFile(locationCsvFile);
                files.add(uri);
                uri = Uri.fromFile(accelerometerCsvFile);
                files.add(uri);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            } else if (locationCsvExists && !accelerometerCsvExists) {
                shareIntent = new Intent(Intent.ACTION_SEND);
                Uri uri = Uri.fromFile(locationCsvFile);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            } else if (!locationCsvExists && accelerometerCsvExists) {
                shareIntent = new Intent(Intent.ACTION_SEND);
                Uri uri = Uri.fromFile(accelerometerCsvFile);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            } else {
                showToast(mMainActivity.getString(R.string.no_data_to_share));
                return;
            }

            shareIntent.setType("application/csv");
            mMainActivity.startActivity(Intent.createChooser(shareIntent, "Share Data"));
        }
    }

}