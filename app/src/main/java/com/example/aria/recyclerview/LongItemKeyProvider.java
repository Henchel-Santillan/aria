package com.example.aria.recyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class LongItemKeyProvider extends ItemKeyProvider<Long> {

    private final RecyclerView recyclerView;

    public LongItemKeyProvider(RecyclerView recyclerView) {
        super(ItemKeyProvider.SCOPE_MAPPED);
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public Long getKey(int position) throws IllegalStateException {
        if (recyclerView.getAdapter() == null)
            throw new IllegalStateException("RecyclerView adapter has not been set.");
        return recyclerView.getAdapter().getItemId(position);
    }

    @Override
    public int getPosition(@NonNull Long key) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(key);
        return (viewHolder == null) ? RecyclerView.NO_POSITION : viewHolder.getLayoutPosition();
    }

}
