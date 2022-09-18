package com.example.aria.ui;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.R;
import com.example.aria.adapter.AudioRecordAdapter;
import com.example.aria.databinding.FragmentPlaybackListBinding;
import com.example.aria.db.entity.AudioRecord;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.viewmodel.AudioRecordListViewModel;

import java.util.List;

public class PlaybackListFragment extends Fragment implements NameRecordingDialogFragment.NameRecordingDialogFragmentListener {

    private FragmentPlaybackListBinding binding;
    private AudioRecordAdapter adapter;
    private ActionMode actionMode;

    public PlaybackListFragment() {
        super(R.layout.fragment_playback_list);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionMode = null;
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
        adapter = new AudioRecordAdapter();
        recordRecyclerView.setAdapter(adapter);

        adapter.setClickItemListener((scopedView, position) -> {
            // Navigate to the PlaybackFragment

            // Pass Bundles Over, Containing the filePath and the fileName
        });

        adapter.setLongClickItemListener((scopedView, position) -> {
            // Only create an ActionMode using the callback for the CAB if one does not already exist
            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback);
                scopedView.setSelected(true);
            }
        });

        final AudioRecordListViewModel viewModel = new ViewModelProvider(this).get(AudioRecordListViewModel.class);
        subscribeUi(viewModel.getRecords());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        adapter = null;
        super.onDestroyView();
    }

    private void subscribeUi(LiveData<List<AudioRecord>> liveData) {
        liveData.observe(getViewLifecycleOwner(), audioRecords -> {
            if (audioRecords != null) {
                binding.setIsLoading(false);
                adapter.submitList(audioRecords);
            } else binding.setIsLoading(true);

            binding.executePendingBindings();
        });
    }


    //**** Callback for the Contextual Action Bar ****//
    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
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

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.viewholderContextualActionBar_optionEdit:
                    DialogFragment dialog = new NameRecordingDialogFragment();
                    dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
                    actionMode.finish();
                    return true;
                case R.id.viewholderContextualActionBar_optionDelete:
                    // Delete the item from the RecyclerView
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
        // Update the title of the AudioRecord given the selectedPosition

    }
}
