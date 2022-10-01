package com.example.aria.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aria.R;
import com.example.aria.databinding.FragmentAudioRecordDetailsBinding;

public class AudioRecordDetailsFragment extends Fragment {

    private FragmentAudioRecordDetailsBinding binding;

    public AudioRecordDetailsFragment() {
        super(R.layout.fragment_audio_record_details);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAudioRecordDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
