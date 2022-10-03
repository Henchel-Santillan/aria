package com.example.aria.recyclerview;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class LongItemDetail extends ItemDetailsLookup.ItemDetails<Long> {

    private final int position;
    private final Long item;

    public LongItemDetail(final int position, final Long item) {
        this.position = position;
        this.item = item;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return item;
    }

}
