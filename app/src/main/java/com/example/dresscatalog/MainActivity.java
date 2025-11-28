package com.example.dresscatalog; // свой пакет

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.dresscatalog.model.Dress;
import com.example.dresscatalog.network.RetrofitClient;
import com.example.dresscatalog.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DressApi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadDresses();
    }

    private void loadDresses() {
        ApiService api = RetrofitClient.getApi();

        Call<List<Dress>> call = api.getDresses();

        call.enqueue(new Callback<List<Dress>>() {
            @Override
            public void onResponse(Call<List<Dress>> call, Response<List<Dress>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Код ответа: " + response.code());
                    return;
                }

                List<Dress> dresses = response.body();
                if (dresses == null) {
                    Log.e(TAG, "Тело ответа пустое");
                    return;
                }

                // Просто выводим в лог список названий платьев
                for (Dress d : dresses) {
                    Log.i(TAG, d.id + " | " + d.name + " | " + d.category + " | " + d.price);
                }
            }

            @Override
            public void onFailure(Call<List<Dress>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети: " + t.getMessage(), t);
            }
        });
    }
}
