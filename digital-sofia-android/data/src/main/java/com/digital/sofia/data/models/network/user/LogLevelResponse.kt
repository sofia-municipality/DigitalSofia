package com.digital.sofia.data.models.network.user

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class LogLevelResponse (
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
    @SerializedName("logLevel") val level: Int,
): BaseResponse()
