package com.example.aria.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import com.example.aria.AriaApplication;
import com.example.aria.db.AudioRecordRepository;
import com.example.aria.db.entity.AudioRecord;

import java.util.List;

public class AudioRecordListViewModel extends AndroidViewModel {

    private static final String QUERY_KEY = "QUERY";

    private final SavedStateHandle savedStateHandle;
    private final AudioRecordRepository repository;
    private final LiveData<List<AudioRecord>> records;

    public AudioRecordListViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;

        repository = ((AriaApplication) application).getRepository();

        // The default is the empty query, since Fts config has not been set up with this AudioRecord db
        records = Transformations.switchMap(savedStateHandle.getLiveData("QUERY", null),
                (Function<CharSequence, LiveData<List<AudioRecord>>>) query -> repository.getRecords());
    }

    // At the moment, not necessarily useful
    public void setQuery(CharSequence query) {
        savedStateHandle.set(QUERY_KEY, query);
    }

    public LiveData<List<AudioRecord>> getRecords() {
        return records;
    }

}
