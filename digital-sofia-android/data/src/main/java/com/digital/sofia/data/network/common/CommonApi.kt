package com.digital.sofia.data.network.common

import com.digital.sofia.data.BuildConfig.URL_LOG_LEVEL
import com.digital.sofia.data.BuildConfig.URL_UPDATE_FIREBASE_TOKEN
import com.digital.sofia.data.models.network.base.EmptyResponse
import com.digital.sofia.data.models.network.firebase.FirebaseTokenRequestBody
import com.digital.sofia.data.models.network.user.LogLevelResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommonApi {

    @POST(URL_UPDATE_FIREBASE_TOKEN)
    suspend fun updateFirebaseToken(
        @Body requestBody: FirebaseTokenRequestBody
    ): Response<EmptyResponse>

    @GET(URL_LOG_LEVEL)
    suspend fun getLogLevel(
        @Query("personIdentifier") personalIdentifier: String
    ): Response<LogLevelResponse>

}