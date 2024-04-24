package com.digital.sofia.data.models.network.confirmation

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class ConfirmationGenerateCodeResponse(
    @SerializedName("expiresIn") val expiresIn: String?,
    @SerializedName("codeAlreadySent") val codeAlreadySent: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()
