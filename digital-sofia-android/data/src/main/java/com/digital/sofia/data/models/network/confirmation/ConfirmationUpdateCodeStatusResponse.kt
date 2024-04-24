package com.digital.sofia.data.models.network.confirmation

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class ConfirmationUpdateCodeStatusResponse(
    @SerializedName("codeUpdated") val codeUpdated: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()
