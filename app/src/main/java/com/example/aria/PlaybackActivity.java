package com.example.aria;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.aria.databinding.ActivityPlaybackBinding;
import com.example.aria.mediaplayer.AudioRecordPlayerService;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;

public class PlaybackActivity extends AppCompatActivity {

    private ActivityPlaybackBinding binding;
    private AudioRecordPlayerService playerService;

    private boolean isServiceBound;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_playback);

        // Note that isServiceBound is always false in onCreate
        isServiceBound = false;

        // Check READ_PHONE_STATE Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Intent playerServiceIntent = new Intent(this, AudioRecordPlayerService.class);
            startService(playerServiceIntent);
            bindService(playerServiceIntent, connection, Context.BIND_AUTO_CREATE);

        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);
        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // Provides callbacks for service binding, which is passed to bindService()
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioRecordPlayerService.AudioRecordPlayerBinder binder = (AudioRecordPlayerService.AudioRecordPlayerBinder) iBinder;
            playerService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    // Activity Result Launcher for READ_PHONE_STATE
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            // Show an AlertDialog explaining to the user why reading phone state is necessary
            DialogFragment dialog = PermissionContextDialogFragment.newInstance(R.string.permissionContextDialog_messagePhoneState);
            dialog.show(getSupportFragmentManager(), PermissionContextDialogFragment.TAG);
        }
    });
}
