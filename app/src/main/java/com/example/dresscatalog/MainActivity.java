package com.example.dresscatalog;

import android.os.Bundle;
import android.text.TextUtils;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.dresscatalog.db.FavoritesStore;
import com.example.dresscatalog.model.Dress;
import com.example.dresscatalog.network.ApiService;
import com.example.dresscatalog.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class  MainActivity extends AppCompatActivity {

    private static final String TAG = "DressApi";

    // UI
    private RecyclerView recycler;
    private TextView tvState;
    private ChipGroup chipGroup;
    private DressAdapter adapter;

    // Data
    private final List<Dress> fullList = new ArrayList<>();
    private final List<Dress> currentList = new ArrayList<>();

    // Filters
    private String categoryFilter = "all"; // all / wedding / evening
    private String currentQuery = "";
    private boolean showOnlyFavorites = false;

    // Sort
    private enum SortMode { NONE, PRICE_ASC, PRICE_DESC, TITLE }
    private SortMode sortMode = SortMode.NONE;

    // Favorites (SQLite)
    private FavoritesStore favoritesStore;
    private Set<Integer> favoriteIds;

    private MenuItem favoritesMenuItem;

    private ActivityResultLauncher<android.content.Intent> detailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Views
        recycler = findViewById(R.id.recycler);
        tvState = findViewById(R.id.tvState);
        chipGroup = findViewById(R.id.chipGroup);

        // Favorites store
        favoritesStore = new FavoritesStore(this);
        favoriteIds = favoritesStore.getAllFavoriteIds();

        // Adapter
        adapter = new DressAdapter(
                dress -> {
                    android.content.Intent i = new android.content.Intent(this, DressDetailActivity.class);
                    i.putExtra(DressDetailActivity.EXTRA_DRESS, dress);
                    detailLauncher.launch(i);
                },
                dress -> {
                    favoritesStore.toggle(dress.id);
                    favoriteIds = favoritesStore.getAllFavoriteIds();
                    adapter.setFavoriteIds(favoriteIds);
                    applyFiltersAndSort();
                }
        );


        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Когда вернулись из деталей — обновим избранное и перерисуем список
                    favoriteIds = favoritesStore.getAllFavoriteIds();
                    adapter.setFavoriteIds(favoriteIds);
                    applyFiltersAndSort();
                }
        );


        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);

        adapter.setFavoriteIds(favoriteIds);

        // Chips
        setupChips();

        // Load
        loadDresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // на будущее (после DetailActivity) — чтобы иконки обновлялись
        refreshFavoritesFromDb();
        applyFiltersAndSort();
    }

    private void refreshFavoritesFromDb() {
        favoriteIds = favoritesStore.getAllFavoriteIds();
        adapter.setFavoriteIds(favoriteIds);
        // если меню уже создано — иконка тоже должна быть актуальной
        updateFavoritesIcon();
    }

    private void setupChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipWedding) categoryFilter = "wedding";
            else if (checkedId == R.id.chipEvening) categoryFilter = "evening";
            else categoryFilter = "all";
            applyFiltersAndSort();
        });
    }

    private void setState(String text, boolean visible) {
        tvState.setText(text);
        tvState.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void loadDresses() {
        setState("Загрузка...", true);

        ApiService api = RetrofitClient.getApi();
        api.getDresses().enqueue(new Callback<List<Dress>>() {
            @Override
            public void onResponse(Call<List<Dress>> call, Response<List<Dress>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Код ответа: " + response.code());
                    setState("Ошибка сервера: " + response.code(), true);
                    return;
                }

                List<Dress> dresses = response.body();
                if (dresses == null) {
                    Log.e(TAG, "Тело ответа пустое");
                    setState("Пустой ответ сервера", true);
                    return;
                }

                fullList.clear();
                fullList.addAll(dresses);

                setState("", false);

                // вдруг в БД уже есть избранные — сразу обновим
                refreshFavoritesFromDb();
                applyFiltersAndSort();
            }

            @Override
            public void onFailure(Call<List<Dress>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети: " + t.getMessage(), t);
                setState("Ошибка сети: " + t.getMessage(), true);
            }
        });
    }

    // -------------------- MENU --------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        favoritesMenuItem = menu.findItem(R.id.action_favorites);
        updateFavoritesIcon();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint("Поиск по названию / SKU / цвету");

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) { return true; }

            @Override
            public boolean onQueryTextChange(String s) {
                currentQuery = s == null ? "" : s.trim();
                applyFiltersAndSort();
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override public boolean onMenuItemActionExpand(@NonNull MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                currentQuery = "";
                applyFiltersAndSort();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            return true;
        }


        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateFavoritesIcon() {
        if (favoritesMenuItem == null) return;
        favoritesMenuItem.setIcon(showOnlyFavorites
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite_border);
    }

    private void showSortDialog() {
        final String[] options = new String[]{
                "Без сортировки",
                "Цена: по возрастанию",
                "Цена: по убыванию",
                "По названию (А-Я)"
        };

        int checked = 0;
        switch (sortMode) {
            case NONE: checked = 0; break;
            case PRICE_ASC: checked = 1; break;
            case PRICE_DESC: checked = 2; break;
            case TITLE: checked = 3; break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Сортировка")
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    if (which == 0) sortMode = SortMode.NONE;
                    if (which == 1) sortMode = SortMode.PRICE_ASC;
                    if (which == 2) sortMode = SortMode.PRICE_DESC;
                    if (which == 3) sortMode = SortMode.TITLE;
                })
                .setPositiveButton("Применить", (dialog, which) -> applyFiltersAndSort())
                .setNegativeButton("Отмена", null)
                .show();
    }

    // -------------------- FILTER + SORT --------------------

    private void applyFiltersAndSort() {
        currentList.clear();
        currentList.addAll(fullList);

        // 1) category
        if (!"all".equals(categoryFilter)) {
            currentList.removeIf(d ->
                    d.category == null || !d.category.equalsIgnoreCase(categoryFilter)
            );
        }

        // 2) favorites
        if (showOnlyFavorites) {
            currentList.removeIf(d -> favoriteIds == null || !favoriteIds.contains(d.id));
        }

        // 3) search: title / sku / color
        if (!TextUtils.isEmpty(currentQuery)) {
            String q = currentQuery.toLowerCase().trim();
            currentList.removeIf(d -> {
                String title = safe(d.title).toLowerCase();
                String sku = safe(d.sku).toLowerCase();
                String color = safe(d.color).toLowerCase();
                return !(title.contains(q) || sku.contains(q) || color.contains(q));
            });
        }

        // 4) sort
        switch (sortMode) {
            case PRICE_ASC:
                Collections.sort(currentList, Comparator.comparingInt(o -> o.priceSom));
                break;
            case PRICE_DESC:
                Collections.sort(currentList, (a, b) -> Integer.compare(b.priceSom, a.priceSom));
                break;
            case TITLE:
                Collections.sort(currentList, (a, b) -> safe(a.title).compareToIgnoreCase(safe(b.title)));
                break;
            case NONE:
            default:
                break;
        }

        if (currentList.isEmpty() && !fullList.isEmpty()) {
            setState("Ничего не найдено", true);
        } else {
            setState("", false);
        }

        adapter.submitList(currentList);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
