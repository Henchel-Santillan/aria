package com.example.aria.ui.fragment;

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
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.R;
import com.example.aria.adapter.AudioRecordAdapter;
import com.example.aria.databinding.FragmentPlaybackListBinding;
import com.example.aria.db.entity.AudioRecord;
import com.example.aria.recyclerview.LongItemDetailsLookup;
import com.example.aria.recyclerview.LongItemKeyProvider;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.viewmodel.AudioRecordListViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class PlaybackListFragment extends Fragment implements NameRecordingDialogFragment.NameRecordingDialogFragmentListener {

    private AudioRecordAdapter adapter;
    private AudioRecordListViewModel viewModel;

    private FragmentPlaybackListBinding binding;
    private ActionMode actionMode;
    private SelectionTracker<Long> selectionTracker;

    public PlaybackListFragment() {
        super(R.layout.fragment_playback_list);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionMode = null;
        viewModel = new ViewModelProvider(requireActivity()).get(AudioRecordListViewModel.class);

        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }
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

        recordRecyclerView.setItemAnimator(new DefaultItemAnimator());
        recordRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        // Create an ItemTouchHelper to facilitate swipe-delete action
        ItemTouchHelper helper = new ItemTouchHelper(   // TODO: Future release: define dragDirs
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;   // TODO: Future release: support item drag reordering
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // TODO: Future release: left -> Add to favourites, right -> Delete
                int position = viewHolder.getLayoutPosition();
                AudioRecord toDelete = adapter.getRecordAt(position);
                viewModel.deleteRecord(adapter.getRecordAt(position));

                // Show a Snackbar indicating that the record was deleted, with the option to undo the delete action
                showSingleDeleteUndoSnackbar(toDelete);
            }
        });

        helper.attachToRecyclerView(recordRecyclerView);

        // Create RecyclerView SelectionTracker to better facilitate click and long click actions
        selectionTracker = new SelectionTracker.Builder<>(
                "audio_tracker",
                recordRecyclerView,
                new LongItemKeyProvider(recordRecyclerView),
                new LongItemDetailsLookup(recordRecyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .withOnItemActivatedListener((item, e) -> true)
                .build();

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
             public void onSelectionChanged() {
                 int selected = selectionTracker.getSelection().size();
                 if (selected > 0) {
                    if (actionMode == null)
                        actionMode = requireActivity().startActionMode(actionModeCallback);
                    actionMode.setTitle(selected + " selected");

                    if (selected > 1) {
                        // TODO: Hide the Delete and Info Actions
                    }
                 } else {
                     actionMode.finish();
                     actionMode = null;
                 }
             }
         });

        adapter.setSelectionTracker(selectionTracker);

        subscribeUi(viewModel.getRecords());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        selectionTracker.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        adapter = null;
        super.onDestroyView();
    }

    private void subscribeUi(@NonNull LiveData<List<AudioRecord>> liveData) {
        liveData.observe(getViewLifecycleOwner(), audioRecords -> {
            if (audioRecords != null) {
                binding.setIsLoading(false);
                adapter.submitList(audioRecords);
            } else binding.setIsLoading(true);

            binding.executePendingBindings();
        });
    }

    // TODO: Implement multiple selection and set CAB title accordingly
    //**** Callback for the Contextual Action Bar ****//
    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.viewholder_contextual_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;   // No-op
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(ActionMode mode, @NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.viewHolderContextualActionBar_optionInfo:
                    // TODO: Launch a "DetailsDialog"
                    mode.finish();
                    return true;
                case R.id.viewholderContextualActionBar_optionEdit:
                    DialogFragment dialog = new NameRecordingDialogFragment();
                    dialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);
                    mode.finish();
                    return true;
                case R.id.viewholderContextualActionBar_optionDelete:
                    // Delete all selected
                    for (long selectionId : selectionTracker.getSelection()) {
                        int position = (int) selectionId;
                        viewModel.deleteRecord(adapter.getRecordAt(position));
                    }
                    mode.finish();
                    return true;
                default: return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionTracker.clearSelection();
            actionMode = null;
        }
    };

    @Override
    public void onNameRecordSave(String name) {
        // Update the title of the AudioRecord at the selected position
        /*AudioRecord record = adapter.getRecordAt();
        record.title = name;
        viewModel.updateRecord(record);*/
    }

    private void showSingleDeleteUndoSnackbar(AudioRecord removed) {
        Snackbar snackbar = Snackbar.make(binding.playbackListFragmentRecordRecyclerView,
                R.string.playbackListFragment_recordDelete, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.playbackListFragment_undoDelete, (scopedView) -> viewModel.insertRecord(removed));
        snackbar.show();
    }

    private void showMultiDeleteUndoSnackbar() {

    }

}
