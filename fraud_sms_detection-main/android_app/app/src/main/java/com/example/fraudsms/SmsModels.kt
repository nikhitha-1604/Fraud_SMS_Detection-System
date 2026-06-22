package com.example.fraudsms

import com.google.gson.annotations.SerializedName

data class SmsRequest(
    val text: String
)

data class SmsResponse(
    val label: String,
    @SerializedName("spam_probability")
    val spamProbability: Double
)

