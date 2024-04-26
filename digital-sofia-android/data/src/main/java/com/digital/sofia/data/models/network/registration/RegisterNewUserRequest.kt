package com.digital.sofia.data.models.network.registration

import com.google.gson.annotations.SerializedName

data class RegisterNewUserRequest(
    @SerializedName("personIdentifier") val personalIdentificationNumber: String
)