package com.digital.sofia.data.repository.network.common

import com.digital.sofia.data.mappers.network.firebase.request.FirebaseTokenRequestMapper
import com.digital.sofia.data.mappers.network.settings.response.LogLevelResponseMapper
import com.digital.sofia.data.network.common.CommonApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.firebase.FirebaseTokenRequestModel
import com.digital.sofia.domain.repository.network.common.CommonNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CommonNetworkRepositoryImpl(
    private val commonApi: CommonApi,
    private val firebaseTokenRequestMapper: FirebaseTokenRequestMapper,
    private val logLevelResponseMapper: LogLevelResponseMapper,
    private val authorizationHelper: AuthorizationHelper,
    coroutineContextProvider: CoroutineContextProvider,
) : CommonNetworkRepository,
    BaseRepository(
        coroutineContextProvider = coroutineContextProvider,
    ) {

    companion object {
        private const val TAG = "CommonNetworkRepository"
    }

    override suspend fun refreshAccessToken() {
        authorizationHelper.startAuthorization()
    }

    override fun updateFirebaseToken(data: FirebaseTokenRequestModel) =
        flow {
            logDebug("updateFirebaseToken", TAG)
            getResult {
                commonApi.updateFirebaseToken(
                    requestBody = firebaseTokenRequestMapper.map(data)
                )
            }.onLoading {
                logDebug("updateFirebaseToken onLoading", TAG)
            }.onSuccess {
                logDebug("updateFirebaseToken onSuccess", TAG)
                emit(ResultEmittedData.success(Unit))
            }.onRetry {
                logDebug("updateFirebaseToken onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("updateFirebaseToken onFailure", it, TAG)
                emit(ResultEmittedData.error(it, null))
            }
        }.flowOn(Dispatchers.IO)

    override fun getLogLevel(personalIdentifier: String) = flow {
        logDebug("getLogLevel", TAG)
        getResult {
            commonApi.getLogLevel(
                personalIdentifier = personalIdentifier,
            )
        }.onLoading {
            logDebug("getLogLevel onLoading", TAG)
        }.onSuccess {
            logDebug("getLogLevel onSuccess", TAG)
            emit(ResultEmittedData.success(logLevelResponseMapper.map(it)))
        }.onRetry {
            logDebug("getLogLevel onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("getLogLevel onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)
}