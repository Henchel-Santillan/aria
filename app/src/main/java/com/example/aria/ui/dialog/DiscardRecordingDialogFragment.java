package com.example.aria.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.aria.R;

public class DiscardRecordingDialogFragment extends DialogFragment {

    public interface DiscardRecordingDialogFragmentListener {
        void onDiscardYesClick();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.discardRecordingDialog_title)
                .setMessage(R.string.discardRecordingDialog_message)
                .setPositiveButton(R.string.discardRecordingDialog_positive, (dialog, which) -> {
                    DiscardRecordingDialogFragmentListener listener = (DiscardRecordingDialogFragmentListener) getParentFragment();
                    if (listener != null)
                        listener.onDiscardYesClick();
                })
                .setNegativeButton(R.string.discardRecordingDialog_negative, (dialog, which) -> { /* No-op */ });
        return builder.create();
    }

    public static String TAG = "DiscardRecordingDialogFragment";
}
