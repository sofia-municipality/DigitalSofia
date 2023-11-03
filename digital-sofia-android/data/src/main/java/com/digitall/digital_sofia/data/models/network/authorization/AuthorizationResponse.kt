package com.digitall.digital_sofia.data.models.network.authorization

import com.digitall.digital_sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class AuthorizationResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: String,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("not-before-policy") val notBeforePolicy: String,
    @SerializedName("session_state") val sessionState: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()