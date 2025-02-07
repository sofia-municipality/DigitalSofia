package com.digital.sofia.data.network.user

import com.digital.sofia.data.BuildConfig.URL_DELETE_USER
import com.digital.sofia.data.BuildConfig.URL_PROFILE_STATUS_TRANSACTION
import com.digital.sofia.data.models.network.base.EmptyResponse
import com.digital.sofia.data.models.network.user.UserProfileStatusChangesRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {

    @DELETE(URL_DELETE_USER)
    suspend fun deleteUser(): Response<EmptyResponse>

    @GET(URL_DELETE_USER)
    suspend fun checkUserForDeletion(): Response<EmptyResponse>

    @POST(URL_PROFILE_STATUS_TRANSACTION)
    suspend fun subscribeForUserProfileStatusChanges(
        @Body requestBody: UserProfileStatusChangesRequestBody
    ): Response<EmptyResponse>
}