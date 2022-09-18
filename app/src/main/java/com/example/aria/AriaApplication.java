package com.example.aria;

import android.app.Application;

import com.example.aria.db.AudioRecordDatabase;
import com.example.aria.db.AudioRecordRepository;

public class AriaApplication extends Application {

    public AudioRecordDatabase getDatabase() {
        return AudioRecordDatabase.getInstance(this);
    }

    public AudioRecordRepository getRepository() {
        return AudioRecordRepository.getInstance(getDatabase());
    }

}
