package com.louisnard.augmentedreality;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import static java.lang.annotation.ElementType.PACKAGE;

/**
 * {@link DialogFragment} that displays an {@link AlertDialog}.
 *
 * @author Alexandre Louisnard
 */

public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    // Tag
    public static final String TAG = AlertDialogFragment.class.getSimpleName();

    // Arguments
    private static final String KEY_TITLE_RES_ID = PACKAGE + ".key.TITLE_RES_ID";
    private static final String KEY_MESSAGE_RES_ID = PACKAGE + ".key.MESSAGE_RES_ID";
    private static final String KEY_POSITIVE_BUTTON_RES_ID = PACKAGE + ".key.POSITIVE_BUTTON_RES_ID";
    private static final String KEY_NEGATIVE_BUTTON_RES_ID = PACKAGE + ".key.NEGATIVE_BUTTON_RES_ID";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment.
     */
    public AlertDialogFragment() {
    }

    /**
     * Factory method to create a new instance of {@link AlertDialogFragment}.
     * @param titleResId the title string resource identifier.
     * @param messageResId the message string resource identifier.
     * @param positiveButtonResId the positive button string resource identifier.
     * @param negativeButtonResId the negative button string resource identifier.
     * @return a new instance of {@link AlertDialogFragment}.
     */
    public static AlertDialogFragment newInstance(int titleResId, int messageResId, int positiveButtonResId, int negativeButtonResId) {
        final AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(KEY_TITLE_RES_ID, titleResId);
        arguments.putInt(KEY_MESSAGE_RES_ID, messageResId);
        arguments.putInt(KEY_POSITIVE_BUTTON_RES_ID, positiveButtonResId);
        arguments.putInt(KEY_NEGATIVE_BUTTON_RES_ID, negativeButtonResId);
        alertDialogFragment.setArguments(arguments);
        return alertDialogFragment;
    }

    /**
     * Factory method to create a new instance of {@link AlertDialogFragment} that displays a simple OK button.
     * @param titleResId the title string resource identifier.
     * @param messageResId the message string resource identifier.
     * @return a new instance of {@link AlertDialogFragment}.
     */
    public static AlertDialogFragment newInstance(int titleResId, int messageResId) {
        return newInstance(titleResId, messageResId, android.R.string.ok, 0);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (arguments.containsKey(KEY_TITLE_RES_ID)) {
            builder.setTitle(arguments.getInt(KEY_TITLE_RES_ID));
        }
        if (arguments.containsKey(KEY_MESSAGE_RES_ID)) {
            builder.setMessage(arguments.getInt(KEY_MESSAGE_RES_ID));
        }
        if (arguments.containsKey(KEY_POSITIVE_BUTTON_RES_ID)) {
            builder.setPositiveButton(arguments.getInt(KEY_POSITIVE_BUTTON_RES_ID), this);
        }
        if (arguments.containsKey(KEY_NEGATIVE_BUTTON_RES_ID)) {
            builder.setNegativeButton(arguments.getInt(KEY_NEGATIVE_BUTTON_RES_ID), this);
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                targetFragment.onActivityResult(getTargetRequestCode(), AppCompatActivity.RESULT_OK, null);
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                targetFragment.onActivityResult(getTargetRequestCode(), AppCompatActivity.RESULT_CANCELED, null);
            }
        }
    }
}
