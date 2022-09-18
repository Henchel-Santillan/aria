package com.example.aria.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.aria.R;
import com.example.aria.databinding.FragmentRecordBinding;
import com.example.aria.ui.dialog.DiscardRecordingDialogFragment;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordFragment extends Fragment implements DiscardRecordingDialogFragment.DiscardRecordingDialogFragmentListener, NameRecordingDialogFragment.NameRecordingDialogFragmentListener, CountUpTimer.OnTimerTickListener {

    private FragmentRecordBinding binding;
    private MediaRecorder recorder;
    private CountUpTimer timer;
    private boolean isRecorderActive, isPaused;

    public RecordFragment() {
        super(R.layout.fragment_record);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRecorderActive = false;
        isPaused = false;
        timer = new CountUpTimer(this);
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
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (!isRecorderActive) {
                    startRecording();
                    showCancelSaveFabs();

                } else {
                    if (!isPaused) pauseRecording();
                    else resumeRecording();
                }
            } else {
                // Ask for the RECORD_AUDIO permission directly
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        binding.fabCancel.setOnClickListener((scopedView) -> {
            pauseRecording();
            DiscardRecordingDialogFragment dialog = new DiscardRecordingDialogFragment();
            dialog.show(getChildFragmentManager(), DiscardRecordingDialogFragment.TAG);
        });

        binding.fabSave.setOnClickListener((scopedView) -> {
            pauseRecording();
            NameRecordingDialogFragment dialog = new NameRecordingDialogFragment();
            dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // TODO:
    // New ActivityResultContracts.RequestMultiplePermissions --> RECORD_AUDIO and WAKE_LOCK
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

    private void hideCancelSaveFabs() {
        binding.fabCancel.animate().translationX(0);
        binding.fabSave.animate().translationX(0);
    }

    @Override
    public void onDiscardYesClick() {
        stopRecording();
        hideCancelSaveFabs();

        Snackbar snackbar = Snackbar.make(binding.fabCancel, R.string.recordFragment_onDiscardYesClick, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.common_actionSnackBar, (view) -> snackbar.dismiss());
        snackbar.show();
    }

    @Override
    public void onNameRecordSave(String name) {
        stopRecording();
        hideCancelSaveFabs();

        final String dialogText = "Recording " + name + " was saved.";
        Snackbar snackbar = Snackbar.make(binding.fabSave, dialogText, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.common_actionSnackBar, (view) -> snackbar.dismiss());
        snackbar.show();
    }


    //*** MediaRecorder Utilities ***//

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        String absolutePathEcd = requireContext().getExternalCacheDir().getAbsolutePath() + "/";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", LocaleListCompat.getDefault().get(0));
        String defaultFileName = dateFormat.format(new Date()) + "_recording";
        recorder.setOutputFile(absolutePathEcd + defaultFileName);

        try {
            recorder.prepare();
        } catch (IOException exception) {
            isRecorderActive = false;
        }

        recorder.start();
        isPaused = false;
        isRecorderActive = true;
        binding.fabRecord.setImageResource(R.drawable.ic_baseline_pause_24);

        timer.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.reset();
        recorder.release();

        recorder = null;
        isPaused = false;
        isRecorderActive = false;
        binding.fabRecord.setImageResource(R.drawable.ic_baseline_mic_24);

        timer.stop();
    }

    private void pauseRecording() {
        recorder.pause();
        isPaused = true;
        binding.fabRecord.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        timer.pause();
    }

    private void resumeRecording() {
        recorder.resume();
        isPaused = false;
        binding.fabRecord.setImageResource(R.drawable.ic_baseline_pause_24);
        timer.start();
    }

    @Override
    public void onTimerTick(String duration) {
        binding.countDownTimer.setText(duration);
    }
}
