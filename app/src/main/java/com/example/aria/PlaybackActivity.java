package com.example.aria;

import android.Manifest;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        // Add support for up navigation in this Activity
        Toolbar toolbar = findViewById(R.id.playbackToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Note that isServiceBound is always false in onCreate
        isServiceBound = false;

        // Get the Intent information from the PlaybackListFragment
        String title = "";
        String filePath = "";
        String amplitudePath = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("title");
            filePath = extras.getString("filePath");
            amplitudePath = extras.getString("amplitudePath");
        }

        // Check READ_PHONE_STATE Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Intent playerServiceIntent = new Intent(this, AudioRecordPlayerService.class);
            playerServiceIntent.putExtra("title", title);
            playerServiceIntent.putExtra("filePath", filePath);
            playerServiceIntent.putExtra("amplitudePath", amplitudePath);
            startService(playerServiceIntent);
            bindService(playerServiceIntent, connection, Context.BIND_AUTO_CREATE);

        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
