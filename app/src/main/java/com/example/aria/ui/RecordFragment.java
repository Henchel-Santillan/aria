package com.example.aria.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.aria.R;
import com.example.aria.databinding.FragmentRecordBinding;
import com.example.aria.ui.dialog.DiscardRecordingDialogFragment;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;
import com.google.android.material.snackbar.Snackbar;

public class RecordFragment extends Fragment implements DiscardRecordingDialogFragment.DiscardRecordingDialogFragmentListener, NameRecordingDialogFragment.NameRecordingDialogFragmentListener {

    FragmentRecordBinding binding;

    public RecordFragment() {
        super(R.layout.fragment_record);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.fabRecord.setOnClickListener((scopedView) -> {
            // Request permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                // Initialize app
                // E.g. enable the record button, setup the MediaPlayer, etc.
                showCancelSaveFabs();

            } else {
                // Ask for the RECORD_AUDIO permission directly
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        binding.fabCancel.setOnClickListener((scopedView) -> {
            // Suspend or pause recording
            // Show the DiscardRecordingFragmentDialog

            DiscardRecordingDialogFragment dialog = new DiscardRecordingDialogFragment();
            dialog.show(getChildFragmentManager(), DiscardRecordingDialogFragment.TAG);
        });

        binding.fabSave.setOnClickListener((scopedView) -> {
            // Stop recording
            // Show the NameRecordingFragmentDialog
            NameRecordingDialogFragment dialog = new NameRecordingDialogFragment();
            dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
        });
    }

    // Request permissions from the user to record voice
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            // Show an AlertDialog explaining to the user why the app requires permission to record audio
            DialogFragment dialog = new PermissionContextDialogFragment();
            dialog.show(getChildFragmentManager(), PermissionContextDialogFragment.TAG);
        }
    });

    private void showCancelSaveFabs() {
        binding.fabCancel.animate().translationX(-getResources().getDimension(R.dimen.standard_105));
        binding.fabSave.animate().translationX(getResources().getDimension(R.dimen.standard_105));
    }

    @Override
    public void onDiscardYesClick() {
        Snackbar snackbar = Snackbar.make(binding.fabCancel, R.string.recordFragment_onDiscardYesClick, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.common_actionSnackBar, (view) -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }

    @Override
    public void onNameRecordSave(String name) {
        final String dialogText = "Recording " + name + " was saved.";
        Snackbar snackbar = Snackbar.make(binding.fabCancel, dialogText, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.common_actionSnackBar, (view) -> {
            snackbar.dismiss();
        });
        snackbar.show();
        // Add badge to the TabLayout with (1) indicating new
    }
}
