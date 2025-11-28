package com.example.dresscatalog.network; // свой пакет

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ⚠️ Важно: обязательно слэш в конце
    private static final String BASE_URL =
            "https://768d857c-d898-40cd-8af3-a4cd0c096f1a.mock.pstmn.io/";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {

            // Логгер запросов/ответов (очень удобно)
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApi() {
        return getInstance().create(ApiService.class);
    }
}
