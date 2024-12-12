package com.example.plantsscanning.api

import com.example.plantsscanning.model.TomatoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("https://my-tomato-api-943797827878.asia-southeast2.run.app/")
    fun uploadImage(
        @Part imageFile: MultipartBody.Part
    ): Call<TomatoResponse>
}


