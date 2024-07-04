package com.example.socialmediaapp.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({"Content-Type: application/json"})

    @POST("v1/projects/socialmediaapp-a4ca5/messages:send")
    Call<Response> sendNotification(@Header("Authorization") String authToken, @Body Sender body);
}
