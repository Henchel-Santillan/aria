package com.example.aria.db;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.aria.db.entity.AudioRecord;

import java.util.List;

public class AudioRecordRepository {

    private static volatile AudioRecordRepository sInstance;

    private final AudioRecordDatabase database;
    private final MediatorLiveData<List<AudioRecord>> observableRecords;

    private AudioRecordRepository(@NonNull final AudioRecordDatabase database) {
        this.database = database;

        observableRecords = new MediatorLiveData<>();
        observableRecords.addSource(database.recordDao().getRecords(), audioRecords -> {
            if (database.isDatabaseBuilt().getValue() != null)
                observableRecords.postValue(audioRecords);
        });
    }

    public static AudioRecordRepository getInstance(final AudioRecordDatabase database) {
        if (sInstance == null) {
            synchronized (AudioRecordRepository.class) {
                if (sInstance == null)
                    sInstance = new AudioRecordRepository(database);
            }
        }
        return sInstance;
    }

    /* NOTE: In practice, having LiveData in the repository does not adhere to the Repository design pattern. */

    public LiveData<List<AudioRecord>> getRecords() {
        return observableRecords;
    }

    public LiveData<AudioRecord> getRecord(final int recordId) {
        return database.recordDao().getRecord(recordId);
    }

    public void updateRecord(final AudioRecord record) {
        database.recordDao().updateRecord(record);
    }

    public void insertRecord(final AudioRecord record) {
        database.recordDao().insertRecord(record);
    }

    public void deleteRecord(final AudioRecord record) {
        database.recordDao().deleteRecord(record);
    }

}
