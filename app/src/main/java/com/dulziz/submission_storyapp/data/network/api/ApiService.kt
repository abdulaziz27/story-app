package com.dulziz.submission_storyapp.data.network.api

import com.dulziz.submission_storyapp.data.network.request.LoginRequest
import com.dulziz.submission_storyapp.data.network.request.RegisterRequest
import com.dulziz.submission_storyapp.data.network.response.GetAllStoryResponse
import com.dulziz.submission_storyapp.data.network.response.LoginResponse
import com.dulziz.submission_storyapp.data.network.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String,
    ): Call<GetAllStoryResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<RegisterResponse>
}