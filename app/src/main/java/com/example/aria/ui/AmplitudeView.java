package com.example.aria.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AmplitudeView extends View {

    private final List<Float> amplitudes;
    private final List<RectF> blocks;

    private final Float radius = 6f;
    private final Float w = 9f;
    private final Float d = 6f;

    private Float screenWidth = 0f;
    private Float screenHeight = 400f;

    private int maxSpikes = 0;

    private final Paint paint;

    public AmplitudeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.amplitudes = new ArrayList<>();
        this.blocks = new ArrayList<>();
        paint = new Paint();
        paint.setColor(Color.rgb(148, 0, 211));

        screenWidth = (float) getResources().getDisplayMetrics().widthPixels;
        maxSpikes = (int) (screenWidth / (w +d));
    }

    public void addAmplitude(Float amplitude) {
        Float norm = (float) Math.min(amplitude.intValue(), 400);
        amplitudes.add(norm);

        blocks.clear();
        List<Float> amps = new ArrayList<>();
        // Takelast in Java
        for (int i = maxSpikes; i < amplitudes.size(); i++) {
            amps.add(amplitudes.get(i));
        }

        for (int i = 0; i < amps.size(); i++) {
            float left = screenWidth - i * (w+d);
            float top = screenHeight / 2 - amps.get(i) / 2;
            float right = left + w;
            float bottom = top+ amps.get(i);

            blocks.add(new RectF(left, top, right, bottom));
        }

        invalidate();
    }

    public List<Float> clear() {
        List<Float> amps = new ArrayList<>(amplitudes);

        amplitudes.clear();
        blocks.clear();
        invalidate();

        return amps;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        for (RectF block : blocks) {
            canvas.drawRoundRect(block, radius, radius, paint);
        }
    }

}
