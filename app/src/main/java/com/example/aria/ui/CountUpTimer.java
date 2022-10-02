package com.example.aria.ui;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.LocaleListCompat;

public class CountUpTimer {

    public interface OnTimerTickListener {
        void onTimerTick(String duration);
    }

    private Long duration, delay;

    private final Handler handler;
    private final Runnable runnable;

    public CountUpTimer(OnTimerTickListener listener) {
        this.duration = 0L;
        this.delay = 100L;

        this.handler = new Handler(Looper.getMainLooper());
        this.runnable = () -> {
            duration += delay;
            start();
            listener.onTimerTick(this.format());
        };
    }

    public void start() {
        handler.postDelayed(runnable, delay);
    }

    public void pause() {
        handler.removeCallbacks(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        duration = 0L;
    }

    public String format() {
        final long milliseconds = (duration % 1000) / 10;
        final long seconds = (duration / 1000) % 60;
        final long minutes = (duration / (1000 * 60)) % 60;
        final long hours = (duration / (1000 * 60 * 60));

        String formatted = "";
        if (hours > 0)
            formatted = String.format(LocaleListCompat.getDefault().get(0), "%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds);
        else
            formatted = String.format(LocaleListCompat.getDefault().get(0), "%02d:%02d.%02d", minutes, seconds, milliseconds);

        return formatted;
    }
}
