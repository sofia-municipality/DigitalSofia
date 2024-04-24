package com.digital.sofia.data.models.network.confirmation

import com.google.gson.annotations.SerializedName

data class ConfirmationUpdateCodeStatusRequestBody (
    @SerializedName("code") val code: String,
    @SerializedName("status") val status: String,
)