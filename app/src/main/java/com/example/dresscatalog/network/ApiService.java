package com.example.dresscatalog.network;

import com.example.dresscatalog.model.Dress;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("dresses")
    Call<List<Dress>> getDresses();

    @GET("dresses/{id}")
    Call<Dress> getDressById(@Path("id") int id);
}
