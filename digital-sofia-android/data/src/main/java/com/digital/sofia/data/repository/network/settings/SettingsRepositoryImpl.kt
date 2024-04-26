/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.settings

import com.digital.sofia.data.mappers.network.settings.request.ChangePinRequestBodyMapper
import com.digital.sofia.data.network.settings.SettingsApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.settings.ChangePinRequestModel
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SettingsRepositoryImpl(
    private val settingsApi: SettingsApi,
    private val changePinRequestBodyMapper: ChangePinRequestBodyMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : SettingsRepository, BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "SettingsRepositoryTag"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun changePin(
        data: ChangePinRequestModel,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("changePin newPin: ${data.pin}", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            settingsApi.changePin(requestBody = changePinRequestBodyMapper.map(data))
        }.onSuccess {
            logDebug("changePin onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("changePin onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("changePin onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteUser(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("deleteUser", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            settingsApi.deleteUser()
        }.onSuccess {
            logDebug("deleteUser onSuccess", TAG)
            emit(ResultEmittedData.success(Unit))
        }.onRetry {
            logDebug("deleteUser onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("deleteUser onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)


}