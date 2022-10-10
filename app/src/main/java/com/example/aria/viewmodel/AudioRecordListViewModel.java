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

    private final AudioRecordRepository repository;
    private final LiveData<List<AudioRecord>> records;

    public AudioRecordListViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);

        repository = ((AriaApplication) application).getRepository();

        // The default is the empty query, since the AudioRecord db does not have Fts configured
        records = Transformations.switchMap(savedStateHandle.getLiveData("QUERY", null),
                (Function<CharSequence, LiveData<List<AudioRecord>>>) query -> repository.getRecords());
    }

    public LiveData<List<AudioRecord>> getRecords() {
        return records;
    }

    public LiveData<List<AudioRecord>> getRecordsByTitle(String titleQuery) {
        return repository.getRecordsByTitle(titleQuery);
    }

    public void updateRecord(AudioRecord record) {
        repository.updateRecord(record);
    }

    public void insertRecord(AudioRecord record) {
        repository.insertRecord(record);
    }

    public void deleteRecord(AudioRecord record) {
        repository.deleteRecord(record);
    }

}
