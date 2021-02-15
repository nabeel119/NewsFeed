package com.example.newsfeed.rest;

import com.example.newsfeed.model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("everything")
    Call<ResponseModel>
    getLatestNews(@Query("q") String q,
                  @Query("from") String from,
                  @Query("sortBy")String sortBy,
                  @Query("apiKey") String apiKey
                 );
}
