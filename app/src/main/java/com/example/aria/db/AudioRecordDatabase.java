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

@Database(entities = {AudioRecord.class}, version = 2, autoMigrations = {@AutoMigration(from = 1, to = 2)})
@TypeConverters(DateConverter.class)
public abstract class AudioRecordDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "audio-record-db";

    public abstract AudioRecordDao recordDao();

    private static volatile AudioRecordDatabase sInstance;
    private final MutableLiveData<Boolean> databaseBuilt = new MutableLiveData<>();

    public static AudioRecordDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AudioRecordDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(), AudioRecordDatabase.class, DATABASE_NAME).build();
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
