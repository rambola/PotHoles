package com.android.rr.potholes.background;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.rr.potholes.potholesconstants.PotHolesConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class BackgroundAcceleratorService extends Service implements SensorEventListener {
    private final String TAG = BackgroundAcceleratorService.class.getSimpleName();
    private SensorManager mSensorManager;
    private NotificationManager mNotificationManager;
    private long mLastUpdatedTime = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(PotHolesConstants.FOREGROUND_ACCELEROMETER_SERVICE_NOTIFICATION_ID,
                getNotification());
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "onStartCommand... intent action: "+intent.getAction());
        if (null != intent.getAction() && intent.getAction().equals(
                PotHolesConstants.ACTION_START_ACCELEROMETER_SENSOR)) {
            if (null != mNotificationManager)
                mNotificationManager.cancel(
                        PotHolesConstants.FOREGROUND_ACCELEROMETER_SERVICE_NOTIFICATION_ID);
            startForegroundAccelerometerService();
        } else if (null != intent.getAction() && intent.getAction().equals(
                PotHolesConstants.ACTION_STOP_ACCELEROMETER_SENSOR)) {
            stopForegroundAccelerometerService();
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e(TAG, "onAccuracyChanged.... sensorType: "+sensor.getName()+
                ", accuracy: "+accuracy);
    }

    private void startForegroundAccelerometerService () {
        PotHolesConstants.isAccelerometerServiceRunning = true;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometerSensor,
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    private void stopForegroundAccelerometerService () {
        PotHolesConstants.isAccelerometerServiceRunning = false;
        if (null != mSensorManager)
            mSensorManager.unregisterListener(this);
        if (null != mNotificationManager)
            mNotificationManager.cancel(
                    PotHolesConstants.FOREGROUND_ACCELEROMETER_SERVICE_NOTIFICATION_ID);

        stopForeground(true);
        stopSelf();
    }

    private void getAccelerometer(SensorEvent event) {
        /*float[] values = event.values;

        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x*x + y*y + z*z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        long actualTime = System.currentTimeMillis();

        if (accelationSquareRoot >= 2) {

            if (actualTime-lastUpdate < 200) {

                return;
            }

            lastUpdate = actualTime;

        }*/

        long currentTimeInMills = System.currentTimeMillis();
        Log.e(TAG, "getAccelerometer.... time diff: "+(currentTimeInMills - mLastUpdatedTime));
        if (currentTimeInMills - mLastUpdatedTime < 200)
            return;

        float xVal = event.values[0];
        float yVal = event.values[1];
        float zVal = event.values[2];

        mLastUpdatedTime = currentTimeInMills;

        String locationUpdate = android.text.format.DateFormat.format(
                "dd-MM-yyyy HH:mm:ss", new Date()).toString() + ", " +
                xVal + ", " + yVal + ", " +zVal;
        Log.i("debug", locationUpdate);
        writeToFile( locationUpdate);
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel(
                "channel_02",
                BackgroundAcceleratorService.class.getSimpleName(),
                NotificationManager.IMPORTANCE_HIGH
        );

        mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_02");
        //builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);

        return builder.build();
    }

    public void writeToFile(String locationData) {
        File myDirectory = new File(Environment.getExternalStorageDirectory(),
                PotHolesConstants.POTHOLES_FOLDER_NAME);

        if (!myDirectory.exists()) {
            myDirectory.mkdir();
        }

        if (myDirectory.exists()) {
            File file = new File(myDirectory, PotHolesConstants.ACCELEROMETER_UPDATES_FILE_NAME);
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

}