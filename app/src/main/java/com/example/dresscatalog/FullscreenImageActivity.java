package com.example.dresscatalog;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_URLS = "extra_urls";
    public static final String EXTRA_POS = "extra_pos";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_images);

        ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_URLS);
        int pos = getIntent().getIntExtra(EXTRA_POS, 0);

        ViewPager2 pager = findViewById(R.id.pagerFullscreen);
        TabLayout dots = findViewById(R.id.dotsFullscreen);
        ImageButton btnClose = findViewById(R.id.btnClose);

        ImagePagerAdapter adapter = new ImagePagerAdapter(position -> {});
        pager.setAdapter(adapter);
        adapter.submit(urls);

        new TabLayoutMediator(dots, pager, (tab, position) -> {}).attach();

        if (pos >= 0) pager.setCurrentItem(pos, false);

        btnClose.setOnClickListener(v -> finish());
    }
}
