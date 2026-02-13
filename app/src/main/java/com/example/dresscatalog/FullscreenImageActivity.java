package com.example.dresscatalog;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        ImageButton btnClose = findViewById(R.id.btnClose);

        List<String> safeUrls = urls != null ? urls : Collections.emptyList();

        ImagePagerAdapter adapter = new ImagePagerAdapter(position -> {});
        pager.setAdapter(adapter);
        adapter.submit(safeUrls);

        if (pos >= 0 && pos < safeUrls.size()) {
            pager.setCurrentItem(pos, false);
        }

        btnClose.setOnClickListener(v -> finish());
    }
}