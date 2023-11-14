package com.digitall.digital_sofia.data.network.authorization

import com.digitall.digital_sofia.data.URL_AUTHORIZATION_ENTER_TO_ACCOUNT
import com.digitall.digital_sofia.data.URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN
import com.digitall.digital_sofia.data.models.network.authorization.AuthorizationResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface AuthorizationApi {

    @FormUrlEncoded
    @POST(URL_AUTHORIZATION_GET_ACCESS_TOKEN_WITH_REFRESH_TOKEN)
    suspend fun getAccessTokenWithRefreshToken(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
    ): retrofit2.Response<AuthorizationResponse>

    @FormUrlEncoded
    @POST(URL_AUTHORIZATION_ENTER_TO_ACCOUNT)
    suspend fun enterToAccount(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("scope") scope: String,
        @Field("pin") pin: String,
        @Field("egn") egn: String,
    ): retrofit2.Response<AuthorizationResponse>

}