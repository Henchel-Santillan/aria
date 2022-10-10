package com.example.aria.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.aria.R;

public class YesNoDialogFragment extends DialogFragment {

    public interface YesNoDialogFragmentListener {
        void onNoClicked();
        void onYesClicked();
    }

    @NonNull
    public static YesNoDialogFragment newInstance(String message) {
        YesNoDialogFragment dialog = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String message = (args == null) ? "" : args.getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.yesNoDialog_positive, (dialog, which) -> {
                    YesNoDialogFragmentListener listener = (YesNoDialogFragmentListener) getParentFragment();
                    if (listener != null)
                        listener.onYesClicked();
                    dismiss();
                })
                .setNegativeButton(R.string.yesNoDialog_negative, (dialog, which) -> {
                   YesNoDialogFragmentListener listener = (YesNoDialogFragmentListener) getParentFragment();
                   if (listener != null)
                       listener.onNoClicked();
                   dismiss();
                });

        return builder.create();
    }

    public static String TAG = "YesNoDialogFragment";

}
