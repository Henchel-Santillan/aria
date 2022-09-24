package com.example.aria.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aria.AriaApplication;
import com.example.aria.db.AudioRecordRepository;
import com.example.aria.db.entity.AudioRecord;

public class AudioRecordViewModel extends AndroidViewModel {

    private final LiveData<AudioRecord> observableRecord;

    public AudioRecordViewModel(@NonNull Application application, final int recordId) {
        super(application);

        AudioRecordRepository repository = ((AriaApplication) application).getRepository();
        this.observableRecord = repository.getRecord(recordId);
    }

    public LiveData<AudioRecord> getRecord() {
        return observableRecord;
    }

}
