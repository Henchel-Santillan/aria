package com.example.aria.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.aria.databinding.FragmentPlaybackBinding;
import com.example.aria.mediaplayer.AudioRecordPlayerService;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;

public class PlaybackFragment extends Fragment {

    private FragmentPlaybackBinding binding;
    private AudioRecordPlayerService audioRecordPlayerService;
    private boolean serviceBound;

    public PlaybackFragment() {
        super(R.layout.fragment_playback);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceBound = false;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);

            if (!serviceBound) {
                Intent playerServiceIntent = new Intent(requireActivity(), AudioRecordPlayerService.class);
                requireActivity().startService(playerServiceIntent);
                requireActivity().bindService(playerServiceIntent, connection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaybackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the AudioRecordPlayerService
        // Stop the AudioRecordPlayerService if it is still active
        if (serviceBound) {
            requireActivity().unbindService(connection);
            serviceBound = false;
        }
    }

    // Provides callbacks for service binding, passed to bindService()
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioRecordPlayerService.AudioRecordPlayerBinder binder = (AudioRecordPlayerService.AudioRecordPlayerBinder) iBinder;
            audioRecordPlayerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    // Request permission to Read Phone State
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            // Show an AlertDialog to the user explaining why reading phone state is necessary
            DialogFragment dialog = PermissionContextDialogFragment.newInstance(R.string.permissionContextDialog_messagePhoneState);
            dialog.show(getChildFragmentManager(), PermissionContextDialogFragment.TAG);
        }
    });

}
