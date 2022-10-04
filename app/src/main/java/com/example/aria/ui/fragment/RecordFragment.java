package com.example.aria.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.aria.R;
import com.example.aria.databinding.FragmentRecordBinding;
import com.example.aria.db.entity.AudioRecord;
import com.example.aria.ui.CountUpTimer;
import com.example.aria.ui.dialog.DiscardRecordingDialogFragment;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;
import com.example.aria.viewmodel.AudioRecordListViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment implements DiscardRecordingDialogFragment.DiscardRecordingDialogFragmentListener, NameRecordingDialogFragment.NameRecordingDialogFragmentListener, CountUpTimer.OnTimerTickListener {

    private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private FragmentRecordBinding binding;
    private boolean isRecorderActive, isPaused;
    private CountUpTimer timer;

    private MediaRecorder recorder;
    private AudioRecordListViewModel viewModel;

    // Important attributes and containers to generate an Audio Record
    private String absolutePathEcd;
    private String fileName;
    private String recordingDuration;
    private List<Float> amplitudes;

    public RecordFragment() {
        super(R.layout.fragment_record);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isRecorderActive = false;
        isPaused = false;
        absolutePathEcd = "";
        fileName = "";
        recordingDuration = "";
        amplitudes = new ArrayList<>();

        timer = new CountUpTimer(this);
        viewModel = new ViewModelProvider(requireActivity()).get(AudioRecordListViewModel.class);
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
            NameRecordingDialogFragment dialog = NameRecordingDialogFragment.newInstance(fileName);
            dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }


    // Request permissions from the user to record voice
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            // Show an AlertDialog explaining to the user why the app requires permission to record audio
            DialogFragment dialog = PermissionContextDialogFragment.newInstance(R.string.permissionContextDialog_messageAudioRecord);
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

        showCloseableSnackbar(binding.fabCancel, getString(R.string.recordFragment_onDiscardYesClick), false);

        // Reset the timer
        binding.countDownTimer.setText(getString(R.string.recordFragment_timerStartText));
    }

    @Override
    public void onNameRecordSave(String name) {
        stopRecording();
        hideCancelSaveFabs();

        String filePath = absolutePathEcd + name;
        String amplitudesPath = filePath.substring(0, filePath.length() - 4);   // .mp4 is 4 characters

        final String dialogText = "Recording " + name + " was saved.";
        showCloseableSnackbar(binding.fabSave, dialogText, true);

        Snackbar snackbar = Snackbar.make(binding.fabSave, dialogText, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.common_actionSnackBar, (view) -> snackbar.dismiss());
        snackbar.show();

        // Reset the timer
        binding.countDownTimer.setText(getString(R.string.recordFragment_timerStartText));

        // Save the waveform generated during recording to a File specified by the "amplitudesPath" path
        try (FileOutputStream fos = new FileOutputStream(amplitudesPath);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {

            out.writeObject(amplitudes);

        } catch (IOException e) {
            Log.e(LOG_TAG, "onNameRecordSave: IOException with either FileOutputStream or ObjectOutputStream", e);
            // TODO: Should show some sort of UI element
        }

        // Construct the saved AudioRecord with the given name and metadata
        // Insert into the database using the ViewModel
        AudioRecord record = new AudioRecord(name, filePath, recordingDuration, amplitudesPath, new Date().getTime());
        viewModel.insertRecord(record);
    }

    private void showCloseableSnackbar(View view, String message, boolean isLong) {
        Snackbar snackbar = Snackbar.make(view, message, isLong? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.common_actionSnackBar, (scopedView) -> snackbar.dismiss());
        snackbar.show();
    }


    //*** MediaRecorder ***//

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        absolutePathEcd = requireContext().getExternalCacheDir().getAbsolutePath() + "/";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", LocaleListCompat.getDefault().get(0));
        fileName = dateFormat.format(new Date()) + "_recording";
        recorder.setOutputFile(absolutePathEcd + fileName);

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

        // Save a copy of the amplitude data in the UI Controller
        // so that the object can be written to a file via an output stream
        amplitudes = binding.amplitudeView.clear();
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


    //*** Count-up Timer ***//

    @Override
    public void onTimerTick(String duration) {
        binding.countDownTimer.setText(duration);
        recordingDuration = duration;
        binding.amplitudeView.addAmplitude((float) recorder.getMaxAmplitude());
    }

}
