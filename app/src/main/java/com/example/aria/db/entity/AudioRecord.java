package com.example.aria.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "audioRecords")
public class AudioRecord {

    @PrimaryKey
    public int recordId;

    public String title;
    public long duration;
    public long dateCreated;

    @Ignore
    public AudioRecord(final int recordId, final String title, final long duration, final long dateCreated) {
        this.recordId = recordId;
        this.title = title;
        this.duration = duration;
        this.dateCreated = dateCreated;
    }
}
