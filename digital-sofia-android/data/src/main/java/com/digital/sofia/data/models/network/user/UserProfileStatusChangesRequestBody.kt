package com.digital.sofia.data.models.network.user

import com.google.gson.annotations.SerializedName

data class UserProfileStatusChangesRequestBody(
    @SerializedName("identification_number") val identificationNumber: String?
)
