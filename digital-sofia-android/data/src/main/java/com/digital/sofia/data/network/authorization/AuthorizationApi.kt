/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.authorization

import com.digital.sofia.data.BuildConfig.URL_AUTHORIZATION_ENTER_TO_ACCOUNT
import com.digital.sofia.data.BuildConfig.URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN
import com.digital.sofia.data.models.network.authorization.AuthorizationResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthorizationApi {

    @FormUrlEncoded
    @POST(URL_AUTHORIZATION_ENTER_TO_ACCOUNT)
    suspend fun enterToAccount(
        @Field("fcm") fcm: String,
        @Field("pin") pin: String,
        @Field("egn") egn: String,
        @Field("scope") scope: String,
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String,
    ): Response<AuthorizationResponse>

    @FormUrlEncoded
    @POST(URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN)
    suspend fun refreshAccessToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("scope") scope: String,
    ): Response<AuthorizationResponse>

}