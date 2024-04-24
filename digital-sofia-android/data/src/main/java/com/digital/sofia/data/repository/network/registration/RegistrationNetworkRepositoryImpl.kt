/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.registration

import com.digital.sofia.data.mappers.network.registration.request.RegisterNewUserRequestMapper
import com.digital.sofia.data.mappers.network.registration.response.CheckPersonalIdentificationNumberResponseMapper
import com.digital.sofia.data.network.registration.RegistrationApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digital.sofia.domain.models.user.RegisterNewUserRequestModel
import com.digital.sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RegistrationNetworkRepositoryImpl(
    private val registrationApi: RegistrationApi,
    private val registerNewUserRequestMapper: RegisterNewUserRequestMapper,
    private val checkPersonalIdentificationNumberResponseMapper: CheckPersonalIdentificationNumberResponseMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : RegistrationNetworkRepository, BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "RegistrationNetworkRepositoryTag"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun registerNewUser(
        data: RegisterNewUserRequestModel
    ): Flow<ResultEmittedData<Unit>> = flow {
        emit(ResultEmittedData.loading(null))
        getResult {
            registrationApi.registerNewUser(
                requestBody = registerNewUserRequestMapper.map(data)
            )
        }.onSuccess {
            logDebug("registerNewUser onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("registerNewUser onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("registerNewUser onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun checkUser(
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>> = flow {
        logDebug("checkUser", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            registrationApi.checkUser(personalIdentificationNumber)
        }.onSuccess {
            val result = checkPersonalIdentificationNumberResponseMapper.map(it)
            logDebug("checkUser onSuccess", TAG)
            emit(ResultEmittedData.success(result))
        }.onRetry {
            logDebug("checkUser onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError(
                "checkUser onFailure error code: ${it.responseCode}", TAG
            )
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

}