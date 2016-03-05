package com.hackathon.igfeels.api;

import com.hackathon.igfeels.instagramApi.MediaResult;
import com.hackathon.igfeels.instagramApi.UserQueryResult;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiInterface {
    @GET("users/search")
    Call<UserQueryResult> getUserId(@Query("q") String username,
                                    @Query("count") int count,
                                    @Query("access_token") String authToken);

    @GET("users/{user-id}/media/recent")
    Call<MediaResult> getUserMedia(@Path("user-id") String userId, @Query("access_token") String accessToken);
}
