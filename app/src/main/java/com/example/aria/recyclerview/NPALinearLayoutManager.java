package com.example.aria.recyclerview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class NPALinearLayoutManager extends LinearLayoutManager {

    public NPALinearLayoutManager(Context context) {
        super(context);
    }

    public NPALinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public NPALinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // Disable predictive animations to prevent RecyclerView.dispatchLayout() from pulling items
    // before calling clearOldPositions()

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

}
