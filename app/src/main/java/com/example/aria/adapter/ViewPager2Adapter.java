package com.example.aria.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.aria.ui.RecordFragment;
import com.example.aria.ui.PlaybackListFragment;

public class ViewPager2Adapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 2;

    public ViewPager2Adapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new RecordFragment() : new PlaybackListFragment();
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
