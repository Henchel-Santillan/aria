package com.example.aria.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.PlaybackActivity;
import com.example.aria.R;
import com.example.aria.adapter.AudioRecordAdapter;
import com.example.aria.databinding.FragmentPlaybackListBinding;
import com.example.aria.db.entity.AudioRecord;
import com.example.aria.recyclerview.LongItemDetailsLookup;
import com.example.aria.recyclerview.LongItemKeyProvider;
import com.example.aria.ui.dialog.AudioRecordDetailsDialogFragment;
import com.example.aria.ui.dialog.NameRecordingDialogFragment;
import com.example.aria.ui.dialog.YesNoDialogFragment;
import com.example.aria.viewmodel.AudioRecordListViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PlaybackListFragment extends Fragment implements NameRecordingDialogFragment.NameRecordingDialogFragmentListener,
        YesNoDialogFragment.YesNoDialogFragmentListener {

    private AudioRecordAdapter adapter;
    private AudioRecordListViewModel viewModel;

    private FragmentPlaybackListBinding binding;
    private ActionMode actionMode;
    private SelectionTracker<Long> selectionTracker;

    private boolean ignoreClick;

    public PlaybackListFragment() {
        super(R.layout.fragment_playback_list);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionMode = null;
        ignoreClick = false;
        viewModel = new ViewModelProvider(requireActivity()).get(AudioRecordListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playback_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Search Functionality
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_playback_list, menu);

                MenuItem searchItem = menu.findItem(R.id.playbackListMenu_optionSearch);
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setSubmitButtonEnabled(false);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (query != null && !query.isEmpty())
                            subscribeSearch(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        if (query != null && !query.isEmpty())
                            subscribeSearch(query);
                        return true;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        // RecyclerView
        RecyclerView recordRecyclerView = binding.playbackListFragmentRecordRecyclerView;
        adapter = new AudioRecordAdapter();
        recordRecyclerView.setAdapter(adapter);

        recordRecyclerView.setItemAnimator(new DefaultItemAnimator());
        recordRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        subscribeUi(viewModel.getRecords());

        // Add Item Touch Listener to the RecyclerView to ignore clicks when the ActionMode is active
        recordRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (actionMode != null)
                    ignoreClick = rv.findChildViewUnder(e.getX(), e.getY()) != null;
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        // Create RecyclerView SelectionTracker to better facilitate click and long click actions
        selectionTracker = new SelectionTracker.Builder<>(
                "audio_tracker",
                recordRecyclerView,
                new LongItemKeyProvider(recordRecyclerView),
                new LongItemDetailsLookup(recordRecyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
             public void onSelectionChanged() {
                 final int selected = selectionTracker.getSelection().size();

                 if (selected > 0) {
                    if (actionMode == null)
                        actionMode = requireActivity().startActionMode(actionModeCallback);

                    actionMode.setTitle(selected + " selected");
                    actionMode.invalidate();

                 } else {
                     actionMode.finish();
                     actionMode = null;
                 }
             }
         });

        adapter.setSelectionTracker(selectionTracker);

        // Add OnItemClickListener to the adapter
        adapter.setOnItemClickListener((scopedView, position) -> {
            if (ignoreClick)
                ignoreClick = false;
            else if (!selectionTracker.hasSelection()) {
                // Get the AudioRecord associated with the position
                AudioRecord record = adapter.getRecordAt(position);

                // Start the PlaybackActivity using an Intent, passing over the paths to the audio and amplitude files
                Intent intent = new Intent(requireActivity(), PlaybackActivity.class);
                intent.putExtra("title", record.title);
                intent.putExtra("filePath", record.filePath);
                intent.putExtra("amplitudePath", record.amplitudePath);
                requireActivity().startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        selectionTracker.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        clearSelection();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        adapter = null;
        clearSelection();
        super.onDestroyView();
    }


    //*** ViewModel ***//

    private void subscribeUi(@NonNull LiveData<List<AudioRecord>> liveData) {
        liveData.observe(getViewLifecycleOwner(), audioRecords -> {
            if (audioRecords != null) {
                binding.setIsLoading(false);
                adapter.submitList(audioRecords);
            } else binding.setIsLoading(true);

            binding.executePendingBindings();
        });
    }

    private void subscribeSearch(String query) {
        viewModel.getRecordsByTitle(query).observe(getViewLifecycleOwner(), audioRecords -> {
            if (audioRecords != null) {
                binding.setIsLoading(false);
                adapter.submitList(audioRecords);
            } else binding.setIsLoading(true);

            binding.executePendingBindings();
        });
    }

    //**** Callback for the Contextual Action Bar ****//
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.viewholder_contextual_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, @NonNull Menu menu) {
            int size = selectionTracker.getSelection().size();
            menu.findItem(R.id.viewholderContextualActionBar_optionEdit).setVisible(size <= 1);
            menu.findItem(R.id.viewHolderContextualActionBar_optionInfo).setVisible(size <= 1);
            return true;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(ActionMode mode, @NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.viewHolderContextualActionBar_optionInfo:
                    long selectedId = selectionTracker.getSelection().iterator().next();
                    AudioRecord record = adapter.getRecordAt((int) selectedId);

                    AudioRecordDetailsDialogFragment detailsDialog = AudioRecordDetailsDialogFragment.newInstance(record.recordId, record.title, record.duration, record.dateCreated);
                    detailsDialog.show(getChildFragmentManager(), AudioRecordDetailsDialogFragment.TAG);

                    mode.finish();
                    return true;
                case R.id.viewholderContextualActionBar_optionEdit:
                    DialogFragment nameDialog = NameRecordingDialogFragment.newInstance(getString(R.string.playbackListFragment_recordNamePlaceholder));
                    nameDialog.show(getChildFragmentManager(), NameRecordingDialogFragment.TAG);

                    // Note that actionMode.finish() is called in onNameRecordSave()

                    return true;
                case R.id.viewholderContextualActionBar_optionDelete:
                    // Explain to the user that this action cannot be undone
                    DialogFragment yesNoDialog = YesNoDialogFragment.newInstance(getString(R.string.playbackListFragment_warningDelete));
                    yesNoDialog.show(getChildFragmentManager(), YesNoDialogFragment.TAG);

                    // Either onNoClicked() or onYesClicked(). Note that actionMode.finish() is called in onYesClicked()

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
        long selectedId = selectionTracker.getSelection().iterator().next();
        AudioRecord record = adapter.getRecordAt((int) selectedId);

        record.title = name;
        viewModel.updateRecord(record);

        // Show a snackbar
        String message = "Recording name changed to " + name + ".";
        Snackbar snackbar = Snackbar.make(binding.playbackListFragmentRecordRecyclerView,
                message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.common_actionSnackBar, (scopedView) -> snackbar.dismiss());
        snackbar.show();

        actionMode.finish();
    }

    @Override
    public void onNoClicked() { /* No-op */ }

    // YesNoDialog: if "Yes" is clicked, delete all selected records
    @Override
    public void onYesClicked() {
        final List<AudioRecord> deletedList = new ArrayList<>();

        for (long selectedId : selectionTracker.getSelection()) {
            AudioRecord toDelete = adapter.getRecordAt((int) selectedId);
            deletedList.add(toDelete);
            viewModel.deleteRecord(toDelete);
        }

        if (selectionTracker.getSelection().size() == 1)            // One item selected
            showSingleDeleteUndoSnackbar(deletedList.get(0));
        else                                                        // Multiple items selected
            showMultiDeleteUndoSnackbar(deletedList);

        actionMode.finish();
    }

    // TODO: Make a Snackbar "Builder" class to simplify creation without action
    private void showSingleDeleteUndoSnackbar(AudioRecord removed) {
        Snackbar snackbar = Snackbar.make(binding.playbackListFragmentRecordRecyclerView,
                R.string.playbackListFragment_recordDelete, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.playbackListFragment_undoDelete, (scopedView) -> viewModel.insertRecord(removed));
        snackbar.show();
    }

    private void showMultiDeleteUndoSnackbar(List<AudioRecord> removed) {
        Snackbar snackbar = Snackbar.make(binding.playbackListFragmentRecordRecyclerView,
                R.string.playbackListFragment_recordsDelete, Snackbar.LENGTH_SHORT);

        snackbar.setAction(R.string.playbackListFragment_undoDelete, (scopedView) -> {
            for (AudioRecord record : removed)
                viewModel.insertRecord(record);
        });

        snackbar.show();
    }

    private void clearSelection() {
        // If the CAB is still showing or is visible, clear the RecyclerView selection and call finish().
        if (actionMode != null) {
            selectionTracker.clearSelection();
            actionMode.finish();
            actionMode = null;
        }
    }

}
