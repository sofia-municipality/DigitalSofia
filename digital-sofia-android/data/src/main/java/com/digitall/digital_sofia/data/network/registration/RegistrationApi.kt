package com.digitall.digital_sofia.data.network.registration

import com.digitall.digital_sofia.data.URL_REGISTRATION_CHECK_PIN
import com.digitall.digital_sofia.data.URL_REGISTRATION_CHECK_USER
import com.digitall.digital_sofia.data.URL_REGISTRATION_REGISTER_NEW_USER
import com.digitall.digital_sofia.data.models.network.authorization.AuthorizationResponse
import com.digitall.digital_sofia.data.models.network.registration.CheckPersonalIdentificationNumberResponse
import com.digitall.digital_sofia.data.models.network.registration.CheckPinResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface RegistrationApi {

    @FormUrlEncoded
    @POST(URL_REGISTRATION_REGISTER_NEW_USER)
    suspend fun registerNewUser(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("scope") scope: String,
        @Field("pin") pin: String,
        @Field("egn") egn: String,
        @Field("phoneNumber") phoneNumber: String?,
        @Field("email") email: String,
        @Field("fcm") fcm: String,
    ): retrofit2.Response<AuthorizationResponse>

    @GET(URL_REGISTRATION_CHECK_USER)
    suspend fun checkUser(
        @Query("personIdentifier") personalIdentificationNumber: String,
    ): retrofit2.Response<CheckPersonalIdentificationNumberResponse>

    @GET(URL_REGISTRATION_CHECK_PIN)
    suspend fun checkPin(
        @Query("personIdentifier") personalIdentificationNumber: String,
        @Query("pin") pin: String,
    ): retrofit2.Response<CheckPinResponse>

}