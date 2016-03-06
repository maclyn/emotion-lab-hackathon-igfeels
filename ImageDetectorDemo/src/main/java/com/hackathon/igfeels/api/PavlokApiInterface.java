package com.hackathon.igfeels.api;

import com.hackathon.igfeels.instagramApi.MediaResult;
import com.hackathon.igfeels.instagramApi.UserEntry;
import com.hackathon.igfeels.instagramApi.UserProfileResult;
import com.hackathon.igfeels.instagramApi.UserQueryResult;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface PavlokApiInterface {
    @GET("api/{objectId}/{type}/255")
    Call<ResponseBody> sendAlert(@Path("objectId") String objectId,
                                 @Path("type") String type,
                                 @Query("alert") String alert);
}
