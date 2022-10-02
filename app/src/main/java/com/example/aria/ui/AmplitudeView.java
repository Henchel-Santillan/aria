package com.example.aria.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class AmplitudeView extends View {

    private final Paint paint;

    // Waveform Constants
    private final float waveformHeight = 400.0f;
    private final float blockWidth = 9.0f;
    private final float interBlockDistance = 6f;

    private final float screenWidth;
    private final int maxSpikes;

    // Containers for keeping track of the amplitudes and the generated blocks
    private final List<Float> amplitudes;
    private final List<RectF> blocks;


    public AmplitudeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.amplitudes = new ArrayList<>();
        this.blocks = new ArrayList<>();
        paint = new Paint();

        // Forms some kind of purple matching Aria's theme
        paint.setColor(Color.rgb(148, 0, 211));

        screenWidth = (float) getResources().getDisplayMetrics().widthPixels;
        maxSpikes = (int) (screenWidth / (blockWidth + interBlockDistance));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Draw a round rectangle for every block formed in addAmplitude
        final float radius = 6.0f;
        for (RectF block : blocks) {
            canvas.drawRoundRect(block, radius, radius, paint);
        }
    }

    public void addAmplitude(@NonNull Float amplitude) {
        final float norm = (float) Math.min(amplitude.intValue(), waveformHeight);
        amplitudes.add(norm);
        blocks.clear();

        // Take only the last n normalized amplitudes, where n = maxSpikes
        final List<Float> amps = new ArrayList<>();
        for (int i = maxSpikes; i < amplitudes.size(); i++) {
            amps.add(amplitudes.get(i));
        }

        // Generate a block using the dimension constants and amplitude information
        for (int i = 0; i < amps.size(); i++) {
            float left = screenWidth - i * (blockWidth + interBlockDistance);
            float top = (waveformHeight / 2) - (amps.get(i) / 2);
            float right = left + blockWidth;
            float bottom = top + amps.get(i);

            blocks.add(new RectF(left, top, right, bottom));
        }

        invalidate();
    }

    @NonNull
    public List<Float> clear() {
        final List<Float> amps = new ArrayList<>(amplitudes);

        amplitudes.clear();
        blocks.clear();
        invalidate();

        return amps;
    }

}
