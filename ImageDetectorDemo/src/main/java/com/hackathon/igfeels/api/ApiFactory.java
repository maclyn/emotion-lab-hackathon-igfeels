package com.hackathon.igfeels.api;

import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;

public class ApiFactory {
    public static ApiInterface getApi(){
        OkHttpClient client = new OkHttpClient();
        client.setFollowRedirects(false);

        Retrofit ra = new Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.instagram.com/v1/")
                .build();
        return ra.create(ApiInterface.class);
    }
}