package com.example.aria.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.aria.R;

public class PermissionContextDialogFragment extends DialogFragment {

    @NonNull
    public static PermissionContextDialogFragment newInstance(int messageId) {
        PermissionContextDialogFragment dialog = new PermissionContextDialogFragment();
        Bundle args = new Bundle();
        args.putInt("messageId", messageId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int messageId = (args == null) ? R.string.permissionContextDialog_messageDefault
                                       : args.getInt("messageId");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(messageId)
                .setNegativeButton(R.string.permissionContextDialog_negative, (dialog, which) -> dismiss())
                .setPositiveButton(R.string.permissionContextDialog_positive, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });

        return builder.create();
    }

    public static String TAG = "PermissionContextDialogFragment";
}
