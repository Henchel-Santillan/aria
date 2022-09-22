package com.example.aria.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AmplitudeView extends View {

    private Paint paint;
    private final List<Float> amplitudes;
    private final List<RectF> blocks;

    private final float BLOCK_RADIUS = 6f;
    private final float BLOCK_WIDTH = 9f;
    private final float BLOCK_SEPARATION = 6f;
    private final int MAX_BLOCKS = (int) (getDisplayWidth() / (BLOCK_WIDTH + BLOCK_SEPARATION));

    public AmplitudeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(Color.rgb(148, 0, 211));
        amplitudes = new ArrayList<>();
        blocks = new ArrayList<>();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        for (RectF block : blocks) {
            canvas.drawRoundRect(block, BLOCK_RADIUS, BLOCK_RADIUS, paint);
        }
    }

    public void addAmplitude(Float amplitude) {
        float norm = (float) Math.min(amplitude.intValue(), getDisplayWidth());
        amplitudes.add(norm);


        blocks.clear();
        final List<Float> lastAmps = new ArrayList<>();

        for (int i = MAX_BLOCKS; i < amplitudes.size(); ++i)
            lastAmps.add(amplitudes.get(i));

        for (int i = 0; i < lastAmps.size(); ++i) {
            blocks.add(new RectF(getDisplayWidth() - i * (BLOCK_WIDTH + BLOCK_SEPARATION),
                                 0f,
                                 getDisplayWidth() - i * (BLOCK_WIDTH + BLOCK_SEPARATION) + BLOCK_WIDTH,
                                 lastAmps.get(i)));
        }

        invalidate();
    }

    private int getDisplayWidth() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ? Resources.getSystem().getDisplayMetrics().widthPixels
                : Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private int getDisplayHeight() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ? Resources.getSystem().getDisplayMetrics().heightPixels
                : Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}
