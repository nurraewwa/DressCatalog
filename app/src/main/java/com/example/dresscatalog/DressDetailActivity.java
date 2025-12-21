package com.example.dresscatalog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dresscatalog.db.FavoritesStore;
import com.example.dresscatalog.model.Dress;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DressDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DRESS = "extra_dress";

    private FavoritesStore favoritesStore;
    private Dress dress;

    // UI
    private ImageButton btnFav;
    private TextInputEditText etNote;

    private ViewPager2 pagerImages;
    private TabLayout tabDots;
    private ImagePagerAdapter imageAdapter;

    private boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dress_detail);

        favoritesStore = new FavoritesStore(this);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationOnClickListener(v -> finish());

        dress = (Dress) getIntent().getSerializableExtra(EXTRA_DRESS);
        if (dress == null) {
            Toast.makeText(this, "Нет данных платья", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        bindDressData();
        setupActions();
    }

    private void bindViews() {
        // pager + dots
        pagerImages = findViewById(R.id.pagerImages);
        tabDots = findViewById(R.id.tabDots);

        imageAdapter = new ImagePagerAdapter(position -> openFullscreen(position));
        pagerImages.setAdapter(imageAdapter);

        new TabLayoutMediator(tabDots, pagerImages, (tab, position) -> {
            // точки
        }).attach();

        // texts
        TextView tvTitle = findViewById(R.id.tvTitleDetail);
        TextView tvPrice = findViewById(R.id.tvPriceDetail);

        TextView tvSku = findViewById(R.id.tvSkuDetail);
        TextView tvColor = findViewById(R.id.tvColorDetail);
        TextView tvSilhouette = findViewById(R.id.tvSilhouetteDetail);
        TextView tvMaterials = findViewById(R.id.tvMaterialsDetail);
        TextView tvFeatures = findViewById(R.id.tvFeaturesDetail);
        TextView tvStyle = findViewById(R.id.tvStyleDetail);

        btnFav = findViewById(R.id.btnFavDetail);

        etNote = findViewById(R.id.etNote);
        MaterialButton btnSaveNote = findViewById(R.id.btnSaveNote);

        // --- Text bind ---
        tvTitle.setText(safe(dress.title));
        tvPrice.setText(MoneyUtils.formatSom(dress.priceSom));

        tvSku.setText("Артикул: " + safe(dress.sku));
        tvColor.setText("Цвет: " + dashIfEmpty(dress.color));
        tvSilhouette.setText("Силуэт: " + dashIfEmpty(dress.silhouette));
        tvMaterials.setText("Материалы: " + joinOrDash(dress.materials));
        tvFeatures.setText("Особенности: " + joinOrDash(dress.features));
        tvStyle.setText("Стиль: " + joinOrDash(dress.style));

        // --- Notes load ---
        String savedNote = favoritesStore.getNote(dress.id);
        if (savedNote != null) etNote.setText(savedNote);

        // --- Save note ---
        btnSaveNote.setOnClickListener(v -> saveNote());
    }

    private void bindDressData() {
        // 1) список фото: imageUrls -> если пусто, пробуем imageUrl
        List<String> urls = dress.imageUrls;

        if (urls == null || urls.isEmpty()) {
            String single = safeUrl(dress.imageUrl);
            if (isHttp(single)) {
                urls = Collections.singletonList(single);
            } else {
                urls = new ArrayList<>();
            }
        }

        imageAdapter.submit(urls);

        // 2) избранное
        updateFavIcon();
    }

    private void setupActions() {
        // Favorite toggle
        btnFav.setOnClickListener(v -> {
            favoritesStore.toggle(dress.id);
            updateFavIcon();
            changed = true; // важно для обновления списка после возврата
        });
    }

    private void openFullscreen(int position) {
        ArrayList<String> urls = new ArrayList<>(imageAdapter.getUrls());
        if (urls.isEmpty()) return;

        Intent i = new Intent(this, FullscreenImageActivity.class);
        i.putStringArrayListExtra(FullscreenImageActivity.EXTRA_URLS, urls);
        i.putExtra(FullscreenImageActivity.EXTRA_POS, position);
        startActivity(i);
    }

    private void saveNote() {
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

        if (note.isEmpty()) {
            favoritesStore.clearNote(dress.id);
            Toast.makeText(this, "Заметка очищена", Toast.LENGTH_SHORT).show();
        } else {
            favoritesStore.saveNote(dress.id, note);
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        }

        changed = true; // тоже считаем изменением
        updateFavIcon();
    }

    private void updateFavIcon() {
        boolean isFav = favoritesStore.isFavorite(dress.id);
        btnFav.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    @Override
    public void finish() {
        if (changed) setResult(RESULT_OK);
        super.finish();
    }

    // --- helpers ---
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeUrl(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isHttp(String s) {
        return s.startsWith("http://") || s.startsWith("https://");
    }

    private static String dashIfEmpty(String s) {
        String v = safe(s);
        return v.isEmpty() ? "-" : v;
    }

    private static String joinOrDash(List<String> list) {
        if (list == null || list.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s == null) continue;
            s = s.trim();
            if (s.isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(s);
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }
}
