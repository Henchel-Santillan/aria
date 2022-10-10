package com.example.aria.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aria.db.entity.AudioRecord;

import java.util.List;

@Dao
public interface AudioRecordDao {

    @Query("SELECT * FROM audioRecords")
    LiveData<List<AudioRecord>> getRecords();

    @Query("SELECT * FROM audioRecords WHERE title LIKE :titleQuery")
    LiveData<List<AudioRecord>> getRecordsByTitle(String titleQuery);

    @Update
    void updateRecord(AudioRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(AudioRecord record);

    @Delete
    void deleteRecord(AudioRecord record);

}
