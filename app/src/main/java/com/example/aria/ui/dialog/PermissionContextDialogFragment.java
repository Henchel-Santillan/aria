package com.example.aria.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.aria.R;

public class PermissionContextDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.permissionContextDialog_message)
                .setPositiveButton(R.string.permissionContextDialog_positive, (dialog, which) -> {
                    // No-op
                });
        return builder.create();
    }

    public static String TAG = "PermissionContextDialogFragment";
}
