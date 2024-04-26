package com.digital.sofia.data.models.network.confirmation

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class ConfirmationGetCodeStatusResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("expiresIn") val expiresIn: Long?,
    @SerializedName("codeExists") val codeExists: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()
