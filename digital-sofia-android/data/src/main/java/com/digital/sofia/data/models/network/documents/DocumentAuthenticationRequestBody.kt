package com.digital.sofia.data.models.network.documents

import com.google.gson.annotations.SerializedName

data class DocumentAuthenticationRequestBody (
    @SerializedName("pin") val pin: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("fcm") val firebaseToken: String
)