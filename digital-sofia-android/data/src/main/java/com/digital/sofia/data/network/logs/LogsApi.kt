package com.digital.sofia.data.network.logs

import com.digital.sofia.data.BuildConfig
import com.digital.sofia.data.models.network.base.EmptyResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface LogsApi {

    @Multipart
    @POST(BuildConfig.URL_UPLOAD_LOGS)
    suspend fun uploadLogs(
        @Path("personIdentifier") personalIdentifier: String,
        @Part files: List<MultipartBody.Part>
    ): Response<EmptyResponse>
}