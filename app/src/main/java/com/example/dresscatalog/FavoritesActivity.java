package com.example.dresscatalog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dresscatalog.db.FavoritesStore;
import com.example.dresscatalog.model.Dress;
import com.example.dresscatalog.network.ApiService;
import com.example.dresscatalog.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "Favorites";

    private RecyclerView recycler;
    private TextView tvState;
    private DressAdapter adapter;

    private FavoritesStore favoritesStore;
    private Set<Integer> favoriteIds;

    private final List<Dress> fullList = new ArrayList<>();
    private final List<Dress> favList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        MaterialToolbar toolbar = findViewById(R.id.toolbarFav);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recyclerFav);
        tvState = findViewById(R.id.tvStateFav);

        favoritesStore = new FavoritesStore(this);
        favoriteIds = favoritesStore.getAllFavoriteIds();

        adapter = new DressAdapter(
                dress -> {
                    Intent i = new Intent(FavoritesActivity.this, DressDetailActivity.class);
                    i.putExtra(DressDetailActivity.EXTRA_DRESS, dress);
                    startActivity(i);
                },
                dress -> {
                    favoritesStore.toggle(dress.id);
                    refreshFavoritesFromDb();
                    applyFavoritesFilter();
                }
        );

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);

        adapter.setFavoriteIds(favoriteIds);

        loadDresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFavoritesFromDb();
        applyFavoritesFilter();
    }

    private void refreshFavoritesFromDb() {
        favoriteIds = favoritesStore.getAllFavoriteIds();
        adapter.setFavoriteIds(favoriteIds);
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
                    setState("Ошибка сервера: " + response.code(), true);
                    return;
                }

                List<Dress> list = response.body();
                if (list == null) {
                    setState("Пустой ответ сервера", true);
                    return;
                }

                fullList.clear();
                fullList.addAll(list);

                setState("", false);
                applyFavoritesFilter();
            }

            @Override
            public void onFailure(Call<List<Dress>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети: " + t.getMessage(), t);
                setState("Ошибка сети: " + t.getMessage(), true);
            }
        });
    }

    private void applyFavoritesFilter() {
        favList.clear();

        if (favoriteIds == null || favoriteIds.isEmpty()) {
            adapter.submitList(favList);
            setState("Пока нет избранных платьев", true);
            return;
        }

        for (Dress d : fullList) {
            if (favoriteIds.contains(d.id)) {
                favList.add(d);
            }
        }

        if (favList.isEmpty()) setState("Избранное пустое", true);
        else setState("", false);

        adapter.submitList(favList);
    }
}
