package com.digital.sofia.data.models.network.registration

import com.google.gson.annotations.SerializedName

data class ChangePinRequestBody (
    @SerializedName("pin") val pin: String
)
