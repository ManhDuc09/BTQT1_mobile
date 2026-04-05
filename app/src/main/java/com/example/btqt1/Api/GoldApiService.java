package com.example.btqt1.Api;

import com.example.btqt1.Model.GoldHistoryResponse;
import com.example.btqt1.Model.GoldResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface GoldApiService {

    @GET("api/prices")
    Call<GoldResponse> getGoldPrice(
            @Query("type") String type
    );

    @GET("api/prices")
    Call<GoldHistoryResponse> getGoldHistory(
            @Query("type") String type,
            @Query("days") int days
    );
}