package com.hackathon.igfeels.api;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;

public class PavlokApiFactory {
    public static PavlokApiInterface getApi(){
        Retrofit ra = new Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pavlok.herokuapp.com/")
                .build();
        return ra.create(PavlokApiInterface.class);
    }
}