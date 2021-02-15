package com.example.newsfeed.rest;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "https://newsapi.org/v2/";
    private static Retrofit retrofit = null;
    public static Retrofit getClient(){
        if (retrofit==null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        Log.d("response", String.valueOf(retrofit));
        return retrofit;
    }
}
