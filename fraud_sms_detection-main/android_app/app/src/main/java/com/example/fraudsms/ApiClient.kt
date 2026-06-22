package com.example.fraudsms

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Emulator -> host via LAN IP (safer than 10.0.2.2 on some setups).
    // Make sure this matches the Flask startup IP (see "Running on http://<IP>:5000").
    // Retrofit requires the base URL to end with a trailing slash.
    private const val BASE_URL = "http://10.0.2.2:5000/"
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val service: SmsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmsApiService::class.java)
    }
}

