package com.example.aria;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

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
    }
}