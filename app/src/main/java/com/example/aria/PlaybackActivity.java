package com.example.aria;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.aria.databinding.ActivityPlaybackBinding;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;

import java.io.IOException;

public class PlaybackActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {

    // Constants
    private static final String LOG_TAG = PlaybackActivity.class.getSimpleName();

    private static final long DELAY = 1000L;        // One second
    private static final int SEEK_SKIP = 5000;

    private Handler handler;
    private Runnable runnable;

    // UI Controller
    private ActivityPlaybackBinding binding;
    private String amplitudePath;

    // MediaPlayer API
    private String recordTitle, filePath;
    private MediaPlayer mediaPlayer = null;


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


        // Get the Intent information from the PlaybackListFragment

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recordTitle = extras.getString("title");
            filePath = extras.getString("filePath");
            amplitudePath = extras.getString("amplitudePath");
        }

        // Check READ_PHONE_STATE Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Explicitly request for READ_PHONE_STATE_PERMISSION
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);
        }

        // Otherwise if permission given, initialize the MediaPlayer with the given filePath and configure the UI
        mediaPlayer = new MediaPlayer();

        // Set the listeners for completion (media reaches end of playback), error and info, and prepare
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(LOG_TAG, "MediaPlayer failed to set the data source using the requested file path", e);
            mediaPlayer.reset();    // Restore MediaPlayer to IDLE state
        }

        // Set up handler to synchronize the MediaPlayer with the SeekBar
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            binding.playbackFragmentSeekbar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(runnable, DELAY);
        };

        // Add click listeners to the buttons
        binding.playbackFragmentPlayPause.setOnClickListener((view) -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                handler.postDelayed(runnable, DELAY);
                binding.playbackFragmentPlayPause.setBackground(AppCompatResources.getDrawable(this, R.drawable.ic_round_play_circle_24));
            }
            else {
                mediaPlayer.start();
                handler.removeCallbacks(runnable);
                binding.playbackFragmentPlayPause.setBackground(AppCompatResources.getDrawable(this, R.drawable.ic_round_pause_circle_24));
            }
        });

        binding.playbackFragmentBackFive.setOnClickListener((view) -> {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - SEEK_SKIP);
            binding.playbackFragmentSeekbar.setProgress(binding.playbackFragmentSeekbar.getProgress() - SEEK_SKIP);
        });

        binding.playbackFragmentSkipFive.setOnClickListener((view) -> {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + SEEK_SKIP);
            binding.playbackFragmentSeekbar.setProgress(binding.playbackFragmentSeekbar.getProgress() + SEEK_SKIP);
        });

        // Add listener to the SeekBar
        binding.playbackFragmentSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean changed) {
                if (changed)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* No-op */ }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* No-op */ }
        });

        // Set the maximum length of the SeekBar equal to that of the duration of the playing media
        binding.playbackFragmentSeekbar.setMax(mediaPlayer.getDuration());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        binding = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Activity Result Launcher for READ_PHONE_STATE
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            // Show an AlertDialog explaining to the user why reading phone state is necessary
            DialogFragment dialog = PermissionContextDialogFragment.newInstance(R.string.permissionContextDialog_messagePhoneState);
            dialog.show(getSupportFragmentManager(), PermissionContextDialogFragment.TAG);
        }
    });


    //*** MediaPlayer Listeners ***//

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            handler.removeCallbacks(runnable);
            mediaPlayer.stop();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.d("MediaPlayer error: ", "MEDIA ERROR UNSUPPORTED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.d("MediaPlayer error: ", "MEDIA ERROR IO " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer error: ", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer error: ", "MEDIA ERROR UNKNOWN " + extra);
                break;
            default: break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:
                Log.d("MediaPlayer Info: ", "MEDIA INFO AUDIO NOT PLAYING " + extra);
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d("MediaPlayer Info: ", "MEDIA INFO NOT SEEKABLE " + extra);
                break;
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.d("MediaPlayer Info: ", "MEDIA INFO UNKNOWN " + extra);
                break;
            default: break;
        }
        return false;
    }

    @Override
    public void onPrepared(@NonNull MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        handler.postDelayed(runnable, DELAY);
    }

}
