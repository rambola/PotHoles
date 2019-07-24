package com.android.rr.potholes.potholesconstants;

public class PotHolesConstants {
    public static final long SPLASH_SCREEN_DELAY = 2500;
    public static final long LOCATION_INTERVAL = 2000;
    public static final long LOCAL_FASTEST_INTERVAL = 1000;
    public static final int FOREGROUND_LOCATION_SERVICE_NOTIFICATION_ID = 100;
    public static final int FOREGROUND_ACCELEROMETER_SERVICE_NOTIFICATION_ID = 100;
    public static final int MY_APP_PERMISSIONS_REQUEST_CODE = 101;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    public static boolean isLocationServiceRunning = false;
    public static boolean isAccelerometerServiceRunning = false;
    public static final String ACTION_START_LOCATION_SERVICE = "com.android.rr.potholes.ACTION_START_LOCATION_SERVICE";
    public static final String ACTION_STOP_LOCATION_SERVICE = "com.android.rr.potholes.ACTION_STOP_LOCATION_SERVICE";
    public static final String ACTION_SHOW_FOREGROUND_NOTIFICATION = "com.android.rr.potholes.ACTION_SHOW_FOREGROUND_NOTIFICATION";
    public static final String ACTION_DISMISS_FOREGROUND_NOTIFICATION = "com.android.rr.potholes.ACTION_DISMISS_FOREGROUND_NOTIFICATION";
    public static final String ACTION_START_ACCELEROMETER_SENSOR = "com.android.rr.potholes.ACTION_START_ACCELEROMETER_SENSOR";
    public static final String ACTION_STOP_ACCELEROMETER_SENSOR = "com.android.rr.potholes.ACTION_STOP_ACCELEROMETER_SENSOR";
}
