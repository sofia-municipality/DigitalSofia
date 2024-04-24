package com.digital.sofia.data.models.network.firebase

import com.google.gson.annotations.SerializedName

data class FirebaseTokenRequestBody (
    @SerializedName("fcm") val fcm: String
)
