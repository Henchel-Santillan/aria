package com.example.aria;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SeekBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.aria.databinding.ActivityPlaybackBinding;
import com.example.aria.ui.dialog.PermissionContextDialogFragment;

import java.io.IOException;
import java.util.concurrent.Executor;

public class PlaybackActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {

    // Constants
    private static final String LOG_TAG = PlaybackActivity.class.getSimpleName();
    private static final String MEDIA_CHANNEL_ID = "media_channel";
    private static final int NOTIFICATION_ID = 69;

    // UI Controller
    private ActivityPlaybackBinding binding;
    private String amplitudePath;

    // MediaPlayer API
    private String recordTitle, filePath;
    private int savedResumePosition;
    private MediaPlayer mediaPlayer = null;

    // Telephony
    private TelephonyManager telephonyManager;
    private boolean isCallOngoing = false;

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // If permission given, initialize the MediaPlayer with the given filePath and set the UI

            if (mediaPlayer == null)
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

            // Add click listeners to the buttons
            binding.playbackFragmentPlayPause.setOnClickListener((view) -> {
                if (mediaPlayer.isPlaying()) {
                    pause();
                    binding.playbackFragmentPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                }
                else {
                    resume();
                    binding.playbackFragmentPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                }
            });

            binding.playbackFragmentBackFive.setOnClickListener((view) -> {

            });

            binding.playbackFragmentSkipFive.setOnClickListener((view) -> {

            });

            // Add listener to the SeekBar
            binding.playbackFragmentSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int position, boolean changed) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { /* No-op */ }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { /* No-op */ }
            });

            // Create the Notification Channel and the Notification
            createNotificationChannel();
            createNotification();

            // Register the call state listener, which listens to incoming calls and pauses or plays the media accordingly
            registerCallStateListener();

        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        destroyNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
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


    //*** MediaPlayer States ***//

    private void start() {
        if (!mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

    private void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            savedResumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(savedResumePosition);
            mediaPlayer.start();
        }
    }


    //*** MediaPlayer Listeners ***//

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stop();
        destroyNotification();
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
        start();
    }


    //*** Notifications ***//

    @SuppressLint("ObsoleteSdkInt")
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.playbackActivity_notificationName);
            String description = getString(R.string.playbackActivity_notificationDescription);
            int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;

            NotificationChannelCompat channel = new NotificationChannelCompat.Builder(MEDIA_CHANNEL_ID, importance)
                    .setName(name)
                    .setDescription(description)
                    .setShowBadge(false)
                    .build();

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(channel);
        }
    }

    public void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_headphones_24)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(recordTitle);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public void destroyNotification() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.cancel(NOTIFICATION_ID);
    }


    //*** Management of Incoming Calls ***//

    public class TelephoneStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {

        public TelephoneStateCallback() {
            super();
        }

        @Override
        public void onCallStateChanged(int state) {
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    if (mediaPlayer != null) {
                        pause();
                        isCallOngoing = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mediaPlayer != null && isCallOngoing) {
                        isCallOngoing = false;
                        resume();
                    }
                    break;
                default: break;
            }
        }
    }

    private void registerCallStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.registerTelephonyCallback((Executor) runnable -> new Thread(runnable).start(), new TelephoneStateCallback());
    }

}
