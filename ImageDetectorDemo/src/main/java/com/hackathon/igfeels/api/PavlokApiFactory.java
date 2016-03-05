package com.hackathon.igfeels.api;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;

public class PavlokApiFactory {
    public static PavlokApiInterface getApi(){
        OkHttpClient client = new OkHttpClient();
        client.setFollowRedirects(false);

        Retrofit ra = new Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://pavlok.herokuapp.com/")
                .build();
        return ra.create(PavlokApiInterface.class);
    }
}