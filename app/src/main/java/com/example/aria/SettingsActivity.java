package com.example.aria;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.aria.ui.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Add support for up navigation in this Activity
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Add the SettingsFragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsFragment_layout, SettingsFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }
}
