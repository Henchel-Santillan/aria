package com.example.aria.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

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

    private MediaPlayer mediaPlayer;
    private IBinder binder;
    private int savedResumePosition;

    private MediaSession mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaController.TransportControls transportControls;

    private AudioManager audioManager;


    //*** Service Lifecycle Methods ***/

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new AudioRecordPlayerBinder();

        // registerCallStateListener(); --> manage incoming phone calls during playback
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // removeNotification
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.release();
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
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {

        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        return false;   // No-op
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        play();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        // No-op
    }

}
