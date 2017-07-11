package com.louisnard.augmentedreality.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.activities.PointsListActivity;
import com.louisnard.augmentedreality.activities.SettingsActivity;
import com.louisnard.augmentedreality.model.database.ARDbContract;
import com.louisnard.augmentedreality.model.database.ARDbHelper;
import com.louisnard.augmentedreality.model.objects.Point;
import com.louisnard.augmentedreality.model.services.PointService;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Fragment showing the points around the user location using augmented reality over a camera preview.<br>
 *
 * @author Alexandre Louisnard
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, ARDbHelper.ARDbHelperListener, SettingsActivity.BackButtonListener, PointService.GpxParserListener {

    // Tag
    private static final String TAG = SettingsFragment.class.getSimpleName();

    // Request codes
    private static final int REQUEST_PICK_GPX_FILE = 1;
    private static final int REQUEST_STORAGE_READ_WRITE_PERMISSIONS = 2;
    private static final int REQUEST_CLEAR_EXISTING_POINTS_FROM_DB = 3;
    private static final int REQUEST_ADD_POINTS_IN_DB_CONFIRMATION_DIALOG = 4;

    private boolean mFragmentIsPaused;

    // Views
    private ProgressBar mProgressBar;
    private TextView mProgressBarTextView;
    private Button mListCurrentPointsButton;
    private Button mClearExistingPointsButton;
    private Button mImportGpxFileButton;

    // GPX parsing
    List<Point> mParsedPointsList;


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
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBarTextView = (TextView) view.findViewById(R.id.progress_bar_text_view);
        mListCurrentPointsButton = (Button) view.findViewById(R.id.list_current_points_btn);
        mClearExistingPointsButton = (Button) view.findViewById(R.id.clear_existing_points_btn);
        mImportGpxFileButton = (Button) view.findViewById(R.id.import_gpx_file_btn);

        // Listeners
        mListCurrentPointsButton.setOnClickListener(this);
        mClearExistingPointsButton.setOnClickListener(this);
        mImportGpxFileButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFragmentIsPaused = false;
        if (getActivity().getClass() == SettingsActivity.class) {
            ((SettingsActivity) getActivity()).setOnBackPressedListener(this);
        }
    }


    @Override
    public boolean onBackPressed() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            return false;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentIsPaused = true;
        if (getActivity().getClass() == SettingsActivity.class) {
            ((SettingsActivity) getActivity()).setOnBackPressedListener(null);
        }
    }

    // View.OnClickListener implementation
    @Override
    public void onClick(View v) {
        if (R.id.list_current_points_btn == v.getId()) {
            startActivity(new Intent(getContext(), PointsListActivity.class));
        } else if (R.id.clear_existing_points_btn == v.getId()) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.confirm, R.string.settings_clear_existing_points_confirmation_message, android.R.string.ok, android.R.string.cancel);
            alertDialogFragment.setTargetFragment(this, REQUEST_CLEAR_EXISTING_POINTS_FROM_DB);
            alertDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
        } else if (R.id.import_gpx_file_btn == v.getId()) {
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

            // Read GPX file into input stream
            InputStream inputStream = null;
            try {
                inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    alertInvalidGpxFile();
                    return;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Parse the input stream
            showProgressBar(true, getString(R.string.gpx_parsing));
            PointService.getInstance().parseGpxAsynchronously(inputStream, this);
        } else if (REQUEST_CLEAR_EXISTING_POINTS_FROM_DB == requestCode && resultCode == Activity.RESULT_OK) {
            ARDbHelper.getInstance(getContext()).clearTable(ARDbContract.PointsColumns.TABLE_NAME);
        } else if (REQUEST_ADD_POINTS_IN_DB_CONFIRMATION_DIALOG == requestCode && resultCode == Activity.RESULT_OK) {
            ARDbHelper.getInstance(getContext()).addPointsAsynchronously(mParsedPointsList, this);
            showProgressBar(true, getString(R.string.gpx_importing));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void alertInvalidGpxFile() {
        if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
        AlertDialogFragment.newInstance(R.string.error, R.string.gpx_invalid_file_alert_message).show(getFragmentManager(), AlertDialogFragment.TAG);
    }

    private void pickFile() {
        final Intent chooseFile;
        final Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, getString(R.string.gpx_pick_a_file));
        startActivityForResult(intent, REQUEST_PICK_GPX_FILE);
    }

    // PointService.GpxParserListener implementation
    @Override
    public void onGpxParsed(List<Point> parsedPoints) {
        showProgressBar(false, null);
        mParsedPointsList = parsedPoints;
        if (mParsedPointsList == null) {
            alertInvalidGpxFile();
            return;
        }
        int pointsNumber = mParsedPointsList.size();
        if (BuildConfig.DEBUG) Log.d(TAG, "Parsed " + pointsNumber + " points from the GPX file");

        if (pointsNumber == 0) {
            AlertDialogFragment.newInstance(R.string.gpx_parsed_alert_title, R.string.gpx_parsed_no_points_alert_message).show(getFragmentManager(), AlertDialogFragment.TAG);
        } else {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.gpx_parsed_alert_title), String.format(getString(R.string.gpx_parsed_alert_message), pointsNumber), android.R.string.ok, android.R.string.cancel);
            alertDialogFragment.setTargetFragment(this, REQUEST_ADD_POINTS_IN_DB_CONFIRMATION_DIALOG);
            alertDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
        }
    }

    // ARDbHelper.ARDbHelperListener implementation
    @Override
    public void onPointsInserted(long insertedPointsNumber) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Added " + insertedPointsNumber + " points in the database");
        showProgressBar(false, null);
        if (!mFragmentIsPaused) {
            AlertDialogFragment.newInstance(getString(R.string.gpx_parsed_alert_title), String.format(getString(R.string.gpx_points_imported_alert_message), insertedPointsNumber)).show(getFragmentManager(), AlertDialogFragment.TAG);
        }
    }

    /**
     * Shows or hides the {@link ProgressBar}.
     * @param show <b>true</b> to show the progress bar. <b>false</b> to hide it.
     * @param message the message to display in the {@link TextView} associated to the progress bar. <b>null</b> to show the progress bar without any message or when hiding the progress bar.
     */
    private void showProgressBar(boolean show, @Nullable String message) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
            if (message != null) {
                mProgressBarTextView.setText(message);
                mProgressBarTextView.setVisibility(View.VISIBLE);
            }
            if (getActivity() != null) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressBarTextView.setVisibility(View.GONE);
            if (getActivity() != null) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}