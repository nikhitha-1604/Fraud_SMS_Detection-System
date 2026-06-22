package com.example.fraudsms

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SmsApiService {

    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun predictSms(@Body request: SmsRequest): Call<SmsResponse>
}

