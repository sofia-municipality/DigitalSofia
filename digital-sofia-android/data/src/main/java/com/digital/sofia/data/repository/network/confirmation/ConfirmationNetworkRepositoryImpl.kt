/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.confirmation

import com.digital.sofia.data.mappers.network.confirmation.request.ConfirmationUpdateCodeStatusRequestMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationGenerateCodeResponseMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationGetCodeStatusResponseMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationUpdateCodeStatusResponseMapper
import com.digital.sofia.data.network.confirmation.ConfirmationApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusRequestModel
import com.digital.sofia.domain.repository.network.confirmation.ConfirmationNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ConfirmationNetworkRepositoryImpl(
    private val confirmationApi: ConfirmationApi,
    private val confirmationGenerateCodeResponseMapper: ConfirmationGenerateCodeResponseMapper,
    private val confirmationGetCodeStatusResponseMapper: ConfirmationGetCodeStatusResponseMapper,
    private val confirmationUpdateCodeStatusResponseMapper: ConfirmationUpdateCodeStatusResponseMapper,
    private val confirmationUpdateCodeStatusRequestMapper: ConfirmationUpdateCodeStatusRequestMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : ConfirmationNetworkRepository, BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "ConfirmationNetworkRepositoryTag"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun generateCode(personalIdentificationNumber: String) = flow {
        logDebug("generateCode personalIdentificationNumber: $personalIdentificationNumber", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            confirmationApi.generateCode(
                personalIdentificationNumber = personalIdentificationNumber,
            )
        }.onSuccess {
            logDebug("generateCode onSuccess", TAG)
            emit(ResultEmittedData.success(confirmationGenerateCodeResponseMapper.map(it)))
        }.onRetry {
            logDebug("generateCode onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("generateCode onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCodeStatus() = flow {
        logDebug("getCodeStatus", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            confirmationApi.getCodeStatus()
        }.onSuccess {
            logDebug("getCodeStatus onSuccess", TAG)
            emit(ResultEmittedData.success(confirmationGetCodeStatusResponseMapper.map(it)))
        }.onRetry {
            logDebug("getCodeStatus onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("getCodeStatus onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateCodeStatus(data: ConfirmationUpdateCodeStatusRequestModel) = flow {
        logDebug("updateCodeStatus code: ${data.code} status: ${data.status}", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            confirmationApi.updateCodeStatus(
                requestBody = confirmationUpdateCodeStatusRequestMapper.map(data),
            )
        }.onSuccess {
            logDebug("updateCodeStatus onSuccess", TAG)
            emit(ResultEmittedData.success(confirmationUpdateCodeStatusResponseMapper.map(it)))
        }.onRetry {
            logDebug("updateCodeStatus onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("updateCodeStatus onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

}