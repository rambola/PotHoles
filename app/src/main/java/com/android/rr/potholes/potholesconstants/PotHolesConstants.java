package com.android.rr.potholes.potholesconstants;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PotHolesConstants {
    public static final long SPLASH_SCREEN_DELAY = 2500;
    public static final long LOCATION_INTERVAL = 2000;
    public static final long LOCAL_FASTEST_INTERVAL = 1000;
    public static final int FOREGROUND_LOCATION_SERVICE_NOTIFICATION_ID = 100;
    public static final int FOREGROUND_ACCELEROMETER_SERVICE_NOTIFICATION_ID = 100;
    public static final int POTHOLES_PERMISSIONS_REQUEST_CODE = 101;
    public static boolean isLocationServiceRunning = false;
    public static boolean isAccelerometerServiceRunning = false;
    public static final String ACTION_START_LOCATION_SERVICE = "com.android.rr.potholes.ACTION_START_LOCATION_SERVICE";
    public static final String ACTION_STOP_LOCATION_SERVICE = "com.android.rr.potholes.ACTION_STOP_LOCATION_SERVICE";
//    public static final String ACTION_SHOW_FOREGROUND_NOTIFICATION = "com.android.rr.potholes.ACTION_SHOW_FOREGROUND_NOTIFICATION";
//    public static final String ACTION_DISMISS_FOREGROUND_NOTIFICATION = "com.android.rr.potholes.ACTION_DISMISS_FOREGROUND_NOTIFICATION";
    public static final String ACTION_START_ACCELEROMETER_SENSOR = "com.android.rr.potholes.ACTION_START_ACCELEROMETER_SENSOR";
    public static final String ACTION_STOP_ACCELEROMETER_SENSOR = "com.android.rr.potholes.ACTION_STOP_ACCELEROMETER_SENSOR";
    public static final String POTHOLES_FOLDER_NAME = "PotHoles";
    public static final String LOCATION_UPDATES_FILE_NAME = "PotHoles_Location.txt";
    public static final String LOCATION_UPDATES_CSV_FILE_NAME = "PotHoles_Location.csv";
    public static final String ACCELEROMETER_UPDATES_FILE_NAME = "PotHoles_Accelerometer.txt";
    public static final String ACCELEROMETER_UPDATES_CSV_FILE_NAME = "PotHoles_Accelerometer.csv";
    public static final int NO_DATA_FOUND = 1;
    public static final int NO_LOCATION_DATA_FOUND = 2;
    public static final int NO_ACCELEROMETER_DATA_FOUND = 3;
    public static final int UNABLE_TO_CREATE_FILE = 4;
    public static final int EXPORTED_SUCCESSFULLY = 5;
}