package com.example.aria.ui;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.R;
import com.example.aria.adapter.AudioRecordAdapter;
import com.example.aria.databinding.FragmentPlaybackListBinding;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;

public class PlaybackListFragment extends Fragment implements NameRecordingDialogFragment.NameRecordingDialogFragmentListener {

    private FragmentPlaybackListBinding binding;

    public PlaybackListFragment() {
        super(R.layout.fragment_playback_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playback_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recordRecyclerView = binding.playbackListFragmentRecordRecyclerView;
        AudioRecordAdapter adapter = new AudioRecordAdapter();
        recordRecyclerView.setAdapter(adapter);

        adapter.setLongClickItemListener(new AudioRecordAdapter.OnLongClickItemListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                // Open the cab by starting Action Mode
            }
        });

        // TODO: Bind ViewModel to RecyclerView adapter
        // viewModel.getList().observe(this, list -> adapter.submitList(list));
    }


    //**** Callback for the Contextual Action Bar ****//
    ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.viewholder_contextual_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;   // No-op
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.viewholderContextualActionBar_optionEdit:
                    DialogFragment dialog = new NameRecordingDialogFragment();
                    dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
                    actionMode.finish();
                    return true;
                case R.id.viewholderContextualActionBar_optionDelete:
                    // Handle delete
                    actionMode.finish();
                    return true;
                default: return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            actionMode = null;
        }
    };

    @Override
    public void onNameRecordSave(String name) {

    }
}
