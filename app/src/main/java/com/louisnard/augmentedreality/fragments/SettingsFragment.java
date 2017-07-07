package com.louisnard.augmentedreality.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.model.objects.Point;
import com.louisnard.augmentedreality.model.services.PointService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing the points around the user location using augmented reality over a camera preview.<br>
 *
 * @author Alexandre Louisnard
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    // Tag
    private static final String TAG = SettingsFragment.class.getSimpleName();

    // Request codes
    private static final int REQUEST_PICK_GPX_FILE = 1;
    private static final int REQUEST_STORAGE_READ_WRITE_PERMISSIONS = 2;

    // Views
    private Button mImportGpxFileButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        mImportGpxFileButton = (Button) view.findViewById(R.id.import_gpx_file_btn);

        // Listeners
        mImportGpxFileButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // View.OnClickListener implementation
    @Override
    public void onClick(View v) {
        if (R.id.import_gpx_file_btn == v.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_READ_WRITE_PERMISSIONS);
            } else {
                pickFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_WRITE_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            pickFile();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_PICK_GPX_FILE == requestCode && resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            final String mimeType = getContext().getContentResolver().getType(uri);
            if (BuildConfig.DEBUG) Log.d(TAG, "Picked file of type: " + mimeType + " and URI: " + uri.getPath());

            // Check that the file is a GPX file
            if (!uri.getPath().matches(".*\\.gpx$")
                    && !mimeType.equalsIgnoreCase("application/gpx+xml")
                    && !mimeType.equalsIgnoreCase("application/gpx")
                    && !mimeType.equalsIgnoreCase("application/octet-stream")
                    && !mimeType.equalsIgnoreCase("text/plain")) {
                alertInvalidGpxFile();
                return;
            }

            // Parse
            List<Point> mPointsList;
            try {
                final InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                mPointsList = PointService.parseGpx(inputStream);
                if (mPointsList == null) {
                    alertInvalidGpxFile();
                    return;
                }
                // TODO: add points to DB (asynchronously ?)
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void alertInvalidGpxFile() {
        if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
        AlertDialogFragment.newInstance(R.string.error, R.string.settings_not_a_gpx_file_alert_dialog_message).show(getFragmentManager(), AlertDialogFragment.TAG);
    }

    private void pickFile() {
        final Intent chooseFile;
        final Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, getString(R.string.settings_pick_a_gpx_file));
        startActivityForResult(intent, REQUEST_PICK_GPX_FILE);
    }


    /*private String readTextFromUri(Uri uri) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }*/
}
