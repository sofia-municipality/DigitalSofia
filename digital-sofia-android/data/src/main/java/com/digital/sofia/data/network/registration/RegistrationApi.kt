/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.registration

import com.digital.sofia.data.BuildConfig.URL_REGISTRATION_CHECK_USER
import com.digital.sofia.data.BuildConfig.URL_REGISTRATION_NEW_USER
import com.digital.sofia.data.models.network.base.EmptyResponse
import com.digital.sofia.data.models.network.registration.CheckPersonalIdentificationNumberResponse
import com.digital.sofia.data.models.network.registration.RegisterNewUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RegistrationApi {

    @GET(URL_REGISTRATION_CHECK_USER)
    suspend fun checkUser(
        @Query("personIdentifier") personalIdentificationNumber: String,
    ): Response<CheckPersonalIdentificationNumberResponse>

    @POST(URL_REGISTRATION_NEW_USER)
    suspend fun registerNewUser(
        @Body requestBody: RegisterNewUserRequest
    ): Response<EmptyResponse>

}