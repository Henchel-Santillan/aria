package com.example.aria.db;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.aria.db.converter.DateConverter;
import com.example.aria.db.dao.AudioRecordDao;
import com.example.aria.db.entity.AudioRecord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {AudioRecord.class}, version = 3)
@TypeConverters(DateConverter.class)
public abstract class AudioRecordDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "audio-record-db";

    public abstract AudioRecordDao recordDao();

    private static final int THREAD_COUNT = 4;
    public static final ExecutorService dbWriteExecutor = Executors.newFixedThreadPool(THREAD_COUNT);

    private static volatile AudioRecordDatabase sInstance;
    private final MutableLiveData<Boolean> databaseBuilt = new MutableLiveData<>();

    public static AudioRecordDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AudioRecordDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(), AudioRecordDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                    if (context.getApplicationContext().getDatabasePath(DATABASE_NAME).exists())
                        sInstance.databaseBuilt.postValue(true);
                }
            }
        }
        return sInstance;
    }

    public LiveData<Boolean> isDatabaseBuilt() {
        return databaseBuilt;
    }

}
