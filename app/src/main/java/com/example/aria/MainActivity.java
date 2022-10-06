package com.example.aria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.aria.adapter.ViewPager2Adapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure the Action Bar
        Toolbar ariaToolbar = findViewById(R.id.ariaToolbar);
        setSupportActionBar(ariaToolbar);

        // Use TabLayoutMediator to get TabLayout and ViewPager2 to interoperate and to enable swipe gestures
        TabLayout tabLayout = findViewById(R.id.mainActivity_tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.mainActivity_viewPager2);
        viewPager2.setAdapter(new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle()));

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.mainActivityTabLayout_tabRecord);
                    tab.setIcon(R.drawable.ic_baseline_record_voice_over_24);
                    break;
                case 1:
                    tab.setText(R.string.mainActivityTabLayout_tabPlayback);
                    tab.setIcon(R.drawable.ic_baseline_headphones_24);
                    break;
                default:
                    break;
            }
        }).attach();

        // Set up the app bar. It is activity-owned, and only the overflow is (currently) required.
        MenuHost host = MainActivity.this;
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.recordMenu_optionSettings) {// Start the Settings Activity
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

}