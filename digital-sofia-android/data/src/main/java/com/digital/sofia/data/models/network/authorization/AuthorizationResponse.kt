/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.authorization

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class AuthorizationResponse(
    @SerializedName("scope") val scope: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("session_state") val sessionState: String?,
    @SerializedName("not-before-policy") val notBeforePolicy: String?,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Long?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()
