/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.settings

import com.digital.sofia.data.BuildConfig.URL_CHANGE_PIN
import com.digital.sofia.data.BuildConfig.URL_DELETE_USER
import com.digital.sofia.data.models.network.base.BaseResponse
import com.digital.sofia.data.models.network.registration.ChangePinRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SettingsApi {

    @POST(URL_CHANGE_PIN)
    suspend fun changePin(
        @Body requestBody: ChangePinRequestBody,
    ): Response<BaseResponse>

    @POST(URL_DELETE_USER)
    suspend fun deleteUser(): Response<BaseResponse>

}