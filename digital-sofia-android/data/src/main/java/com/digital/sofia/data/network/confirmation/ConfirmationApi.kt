/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.confirmation

import com.digital.sofia.data.BuildConfig.URL_CONFIRMATION_GENERATE_CODE
import com.digital.sofia.data.BuildConfig.URL_CONFIRMATION_GET_CODE_STATUS
import com.digital.sofia.data.BuildConfig.URL_CONFIRMATION_UPDATE_CODE_STATUS
import com.digital.sofia.data.models.network.confirmation.ConfirmationUpdateCodeStatusRequestBody
import com.digital.sofia.data.models.network.confirmation.ConfirmationGenerateCodeResponse
import com.digital.sofia.data.models.network.confirmation.ConfirmationGetCodeStatusResponse
import com.digital.sofia.data.models.network.confirmation.ConfirmationUpdateCodeStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ConfirmationApi {

    @GET(URL_CONFIRMATION_GENERATE_CODE)
    suspend fun generateCode(
        @Query("personIdentifier") personalIdentificationNumber: String,
    ): Response<ConfirmationGenerateCodeResponse>

    @GET(URL_CONFIRMATION_GET_CODE_STATUS)
    suspend fun getCodeStatus(): Response<ConfirmationGetCodeStatusResponse>

    @POST(URL_CONFIRMATION_UPDATE_CODE_STATUS)
    suspend fun updateCodeStatus(
        @Body requestBody: ConfirmationUpdateCodeStatusRequestBody,
    ): Response<ConfirmationUpdateCodeStatusResponse>

}