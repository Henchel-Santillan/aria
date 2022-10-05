package com.example.aria.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.DialogFragment;

import com.example.aria.R;
import com.example.aria.databinding.FragmentDialogAudioRecordDetailsBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecordDetailsDialogFragment extends DialogFragment {

    private FragmentDialogAudioRecordDetailsBinding binding;

    public AudioRecordDetailsDialogFragment() {
        super(R.layout.fragment_dialog_audio_record_details);
    }

    @NonNull
    public static AudioRecordDetailsDialogFragment newInstance(int id, String title, String duration, long dateCreated) {
        AudioRecordDetailsDialogFragment dialog = new AudioRecordDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("title", title);
        args.putString("duration", duration);
        args.putLong("dateCreated", dateCreated);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDialogAudioRecordDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {

            // Retrieve the values from the Bundle
            int recordId = args.getInt("id");
            String recordTitle = args.getString("title");
            String recordDuration = args.getString("duration");
            long recordDateLong = args.getLong("dateCreated");

            // Format the creation date of the record first
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", LocaleListCompat.getDefault().get(0));
            String recordDateString = format.format(new Date(recordDateLong));

            // Set the values of the UI elements
            binding.audioRecordDetailsDialogTitle.setText(getString(R.string.audioRecordDetailsDialog_titleText, recordTitle));
            binding.audioRecordDetailsDialogDuration.setText(getString(R.string.audioRecordDetailsDialog_durationText, recordDuration));
            binding.audioRecordDetailsDialogRecordId.setText(getString(R.string.audioRecordDetailsDialog_recordIdText, recordId));
            binding.audioRecordDetailsDialogDateCreated.setText(getString(R.string.audioRecordDetailsDialog_dateCreatedText, recordDateString));
        }

        binding.audioRecordDetailsDialogDismissButton.setOnClickListener((scopedView) -> dismiss());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public static String TAG = "AudioRecordDetailsDialogFragment";

}
