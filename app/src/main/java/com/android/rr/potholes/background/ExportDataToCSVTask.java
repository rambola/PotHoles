package com.android.rr.potholes.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.rr.potholes.R;
import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.android.rr.potholes.presenters.MainActivityPresenter;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportDataToCSVTask extends AsyncTask<String, Void, String> {
    private Context mContext;
    private MainActivityPresenter mMainActivityPresenter;
    private String mMsg;
    private final ProgressDialog mDialog;
    private final String TAG = ExportDataToCSVTask.class.getSimpleName();

    public ExportDataToCSVTask (Context context, MainActivityPresenter mainActivityPresenter) {
        mContext = context;
        mMainActivityPresenter = mainActivityPresenter;
        mDialog = new ProgressDialog(context);
        mMsg = context.getString(R.string.something_wrong);
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage(mContext.getString(R.string.exporting_data));
        mDialog.setCancelable(false);
        mDialog.show();
    }

    protected String doInBackground(final String... args) {
        try {
            mMsg = writeDataToCSVFile(PotHolesConstants.LOCATION_UPDATES_FILE_NAME,
                    PotHolesConstants.LOCATION_UPDATES_CSV_FILE_NAME,
                    new String[]{"DateTime", "Latitude", "Longitude"});
            return  mMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mMsg;
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i(TAG, "onCancelled is called...");
        if (null!= mDialog && mDialog.isShowing()) { mDialog.dismiss(); }
        mMainActivityPresenter.showToast(mContext.getString(R.string.exporting_cancelled));
    }

    protected void onPostExecute(final String msg) {
        Log.i(TAG, "onPostExecute... msg: "+msg );
        if (null!= mDialog && mDialog.isShowing()) { mDialog.dismiss(); }
        if (!TextUtils.isEmpty(msg))
            mMainActivityPresenter.showToast(msg);
    }

    private String writeDataToCSVFile (String txtFileName, String csvFileName,
                                       String[] columnNames) throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory(),
                PotHolesConstants.POTHOLES_FOLDER_NAME);
        String returnMsg;
        if (!dir.exists()) {
            return mContext.getString(R.string.no_data_found);
        }

        File txtFile = new File(dir, txtFileName);
        Log.e(TAG, "writeDataToCSVFile... txtFileName: "+txtFileName+", txtFile.exists(): "+
                txtFile.exists());
        if (!txtFile.exists() && txtFileName.equals(PotHolesConstants.LOCATION_UPDATES_FILE_NAME)) {
            writeDataToCSVFile(PotHolesConstants.ACCELEROMETER_UPDATES_FILE_NAME,
                    PotHolesConstants.ACCELEROMETER_UPDATES_CSV_FILE_NAME, new String[]{"DateTime",
                            "xVal", "yVal", "zVal"});
            returnMsg = mContext.getString(R.string.no_location_updates);
        } else if (!txtFile.exists() && txtFileName.equals(PotHolesConstants.ACCELEROMETER_UPDATES_FILE_NAME)) {
            returnMsg = mContext.getString(R.string.no_accelerometer_updates);
        } else {
            CSVWriter csvWrite = null;
            List<String[]> strings = new ArrayList<>();
            File csvFile = new File(dir, csvFileName);
            Log.i(TAG, "writeDataToCSVFile....csvFile.exists(): "+csvFile.exists());
            if (!csvFile.exists()) {
                boolean fileCreated = csvFile.createNewFile();
                Log.i(TAG, "writeDataToCSVFile....csvFile.exists(): "+csvFile.exists()+
                        ", fileCreated: "+fileCreated);
                if (fileCreated) {
                    csvWrite = new CSVWriter(new FileWriter(csvFile, true));
                    strings.add(columnNames);
                } else {
                    return mContext.getString(R.string.unable_to_create_file);
                }
            } else {
                csvWrite = new CSVWriter(new FileWriter(csvFile, true));
            }

            try {
                BufferedReader br = new BufferedReader(new FileReader(txtFile));
                String line;

                while ((line = br.readLine()) != null) {
                    String[] splitLine = line.split(",");
                    int numberOfColumns = splitLine.length;
                    String[] myData = new String[numberOfColumns];
                    for (int i = 0; i < numberOfColumns; i++) {
//                        System.arraycopy(splitLine, i, myData, i, numberOfColumns);
                        myData[i] = splitLine[i];
                    }
                    strings.add(myData);
                }
                csvWrite.writeAll(strings);
                csvWrite.flush();
                br.close();
                txtFile.delete();
                csvWrite.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (txtFileName.equals(PotHolesConstants.LOCATION_UPDATES_FILE_NAME)) {
                writeDataToCSVFile(PotHolesConstants.ACCELEROMETER_UPDATES_FILE_NAME,
                        PotHolesConstants.ACCELEROMETER_UPDATES_CSV_FILE_NAME, new String[]{"DateTime",
                                "xVal", "yVal", "zVal"});
            }

            returnMsg = mContext.getString(R.string.data_export_success);
        }
        Log.i(TAG, "before returning returnMsg: "+returnMsg);
        return returnMsg;
    }

}