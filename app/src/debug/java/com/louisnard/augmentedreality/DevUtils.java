package com.louisnard.augmentedreality;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Helper class providing some tools to facilitate Android development.
 *
 * @author Alexandre Louisnard
 * 2017
 */

public class DevUtils {

    // Tag
    public static final String TAG = DevUtils.class.getSimpleName();

    /**
     * Copies the current application database to the external storage.
     * If not granted already, the function will ask for WRITE_EXTERNAL_STORAGE permission et will need to be called again once the permission granted.
     * @param activity the calling activity.
     * @param databaseName the name of the database to copy.
     * @return <b>true</b> if the copy has succeeded. <b>false</b> otherwise.
     */
    public static boolean exportDatabaseToExternalStorage(Activity activity, String databaseName) {
        // Check for external storage write permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            // If external storage write permission has been granted
            try {
                File currentDb = activity.getDatabasePath(databaseName);
                File externalStoragePath = Environment.getExternalStorageDirectory();
                File backupDb = new File(externalStoragePath, "backup_" + databaseName);
                if (currentDb.exists()) {
                    FileChannel src = new FileInputStream(currentDb).getChannel();
                    FileChannel dst = new FileOutputStream(backupDb).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(TAG, "Database copied to: " + backupDb.toString());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
