package com.example.aria.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "audioRecords")
public class AudioRecord {

    @PrimaryKey(autoGenerate = true)
    public int recordId;

    public String title;
    public String filePath;
    public String duration;
    public String amplitudePath;
    public long dateCreated;

    public AudioRecord(final String title, final String filePath, final String duration, final String amplitudePath, final long dateCreated) {
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
        this.amplitudePath = amplitudePath;
        this.dateCreated = dateCreated;
    }

}
