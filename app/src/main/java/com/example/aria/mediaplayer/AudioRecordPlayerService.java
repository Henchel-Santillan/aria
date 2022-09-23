package com.example.aria.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aria.R;
import com.example.aria.db.entity.AudioRecord;

import java.io.IOException;

public class AudioRecordPlayerService extends Service implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    // Custom Binder class that returns the current AudioRecordPlayerService instance, which has public methods that the client can call
    public class AudioRecordPlayerBinder extends Binder {
        public AudioRecordPlayerService getService() {
            return AudioRecordPlayerService.this;
        }
    }

    public enum PlaybackState { RESUMED_PLAYING, PAUSED }

    private static final int NOTIFICATION_ID = 420;
    private static final String MEDIA_CHANNEL_ID = "media_channel";
    private static final String MEDIA_SESSION_TAG = "AudioRecordPlayer";

    private static final String NOTIF_ACTION_PLAY = "com.example.aria.mediaplayer.NOTIF_ACTION_PLAY";
    private static final String NOTIF_ACTION_PAUSE = "com.example.aria.mediaplayer.NOTIF_ACTION_PAUSE";
    private static final String NOTIF_ACTION_STOP = "com.example.aria.mediaplayer.NOTIF_ACTION_STOP";
    private static final String NOTIF_ACTION_NEXT_FIVE = "com.example.aria.mediaplayer.NOTIF_ACTION_NEXT_FIVE";
    private static final String NOTIF_ACTION_PREV_FIVE = "com.example.aria.mediaplayer.NOTIF_ACTION_PREV_FIVE";

    private MediaPlayer mediaPlayer;
    private String pathToAudioFile;         // TODO: Init (via Bundle? or ViewModel)

    private IBinder binder;
    private int savedResumePosition;

    //*** Media Session ***//

    private MediaSession mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaController.TransportControls transportControls;

    //*** Audio Management ***//

    private AudioManager audioManager;
    private AudioAttributes audioAttributes;
    private AudioFocusRequest audioFocusRequest;
    private AudioRecord record;         // TODO: Initialize

    private final Object focusLock = new Object();
    private boolean resumeOnFocusGain;
    private boolean playbackDelayed;



    //*** Service Lifecycle Methods ***/

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new AudioRecordPlayerBinder();
        pathToAudioFile = "";
        savedResumePosition = 0;
        resumeOnFocusGain = false;
        playbackDelayed = false;
        // registerCallStateListener(); --> manage incoming phone calls during playback
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Only need to create NotificationChannel on API 26+ devices
        createNotificationChannel();

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                stopSelf();
            }
            createNotification(PlaybackState.RESUMED_PLAYING);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        destroyNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.release();

        destroyNotification();
    }


    //*** MediaPlayer Methods ***/

    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        // Configure the listeners
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);

        // Guarantees that CPU continues running while MediaPlayer is playing
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.reset();

        // Set the data source of the MediaPlayer to the String representing the AudioRecord's URI
        try {
            mediaPlayer.setDataSource("");
        } catch (IOException e) {
            stopSelf();
        }

        mediaPlayer.prepareAsync();
    }

    private void play() {
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

    private void skipFiveForward() {
        pause();
        mediaPlayer.seekTo(Math.min(savedResumePosition + 5, mediaPlayer.getDuration()));
        play();
    }

    private void skipFiveBackward() {
        pause();
        mediaPlayer.seekTo(savedResumePosition - 5);
    }


    //*** MediaPlayer Listeners ***//

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stop();
        destroyNotification();
        stopSelf();
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
    public void onPrepared(MediaPlayer mediaPlayer) {
        play();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        // No-op
    }


    //*** MediaSession ***//

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager == null) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaSession = new MediaSession(getApplicationContext(), MEDIA_SESSION_TAG);

            transportControls = mediaSession.getController().getTransportControls();

            // Indicate that the media session is ready to receive commits
            mediaSession.setActive(true);

            // Set the media session's metadata
            updateSessionMetaData();

            mediaSession.setCallback(new MediaSession.Callback() {
                @Override
                public void onPlay() {
                    super.onPlay();
                    resume();
                    createNotification(PlaybackState.RESUMED_PLAYING);
                }

                @Override
                public void onPause() {
                    super.onPause();
                    pause();
                    createNotification(PlaybackState.PAUSED);
                }

                @Override
                public void onStop() {
                    super.onStop();
                    destroyNotification();
                    stopSelf();
                }

                @Override
                public void onSeekTo(long position) {
                    super.onSeekTo(position);
                }
            });
        }
    }

    private void updateSessionMetaData() {
        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, record.title)
                .build()
        );
    }


    //*** Audio Management ***//

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build();

        int result = audioManager.requestAudioFocus(audioFocusRequest);
        synchronized (focusLock) {
            switch (result) {
                case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    playbackDelayed = false;
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
                    playbackDelayed = true;
                    break;
                default: break;
            }
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                // Resume playback; app has been granted audio focus gain
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized (focusLock) {
                        resumeOnFocusGain = false;
                        playbackDelayed = false;
                    }

                    if (mediaPlayer == null)
                        initMediaPlayer();
                    else if (mediaPlayer.isPlaying())
                        mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // All transient losses are handled the same way since the playback from this service will never be ducked
                // Permanent loss of audio focus; pause playback and delay 10 seconds before stopping completely
                synchronized (focusLock) {
                    resumeOnFocusGain = focusChange != AudioManager.AUDIOFOCUS_LOSS && mediaPlayer.isPlaying();
                    playbackDelayed = false;
                }

                // Stop the playback and release the MediaPlayer
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            default: break;
        }
    }

    //*** Notifications ***//

    private void createNotification(final PlaybackState state) {
        final int playPauseActionId, playPauseNotifDrawableId;

        if (state == PlaybackState.RESUMED_PLAYING) {
            playPauseActionId = 1;
            playPauseNotifDrawableId = R.drawable.ic_baseline_pause_24;
        } else {
            playPauseActionId = 0;
            playPauseNotifDrawableId = R.drawable.ic_baseline_play_arrow_24;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_headphones_24)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0, 1, 2))
                .setContentTitle(record.title)
                .addAction(R.drawable.ic_baseline_replay_5_24, getString(R.string.audioRecordPlayerService_notificationActionPrevFive), getNotificationAction(2))
                .addAction(playPauseNotifDrawableId, getString(R.string.audioRecordPlayerService_notificationActionPause), getNotificationAction(playPauseActionId))
                .addAction(R.drawable.ic_baseline_forward_5_24, getString(R.string.audioRecordPlayerService_notificationActionNextFive), getNotificationAction(3));

        // Show the notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void destroyNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);

    }

    @Nullable
    private PendingIntent getNotificationAction(final int actionId) {
        Intent intent = new Intent(this, AudioRecordPlayerService.class);

        switch (actionId) {
            case 0:
                intent.setAction(NOTIF_ACTION_PLAY);
                break;
            case 1:
                intent.setAction(NOTIF_ACTION_PAUSE);
                break;
            case 2:
                intent.setAction(NOTIF_ACTION_PREV_FIVE);
                break;
            case 3:
                intent.setAction(NOTIF_ACTION_NEXT_FIVE);
            default: return null;
        }

        return PendingIntent.getService(this, actionId, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.audioRecordPlayerService_notificationName);
            String description = getString(R.string.audioRecordPlayerService_notificationDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(MEDIA_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(false);

            // Register the channel with the system
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

}
