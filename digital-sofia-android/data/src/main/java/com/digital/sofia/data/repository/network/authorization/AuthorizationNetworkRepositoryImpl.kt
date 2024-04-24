/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.network.authorization

import com.digital.sofia.data.BuildConfig.CLIENT_ID
import com.digital.sofia.data.BuildConfig.CLIENT_SCOPE
import com.digital.sofia.data.BuildConfig.GRANT_TYPE_PASSWORD
import com.digital.sofia.data.BuildConfig.GRANT_TYPE_REFRESH_TOKEN
import com.digital.sofia.data.mappers.network.authorization.AuthorizationResponseMapper
import com.digital.sofia.data.network.authorization.AuthorizationApi
import com.digital.sofia.data.repository.network.base.BaseRepository
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthorizationNetworkRepositoryImpl(
    private val authorizationApi: AuthorizationApi,
    private val authorizationResponseMapper: AuthorizationResponseMapper,
    coroutineContextProvider: CoroutineContextProvider,
) : AuthorizationNetworkRepository,
    BaseRepository(
    coroutineContextProvider = coroutineContextProvider,
) {

    companion object {
        private const val TAG = "AuthorizationRepositoryTag"
    }

    override suspend fun refreshAccessToken() {
        // Nothing to implement
    }

    override fun enterToAccount(
        hashedPin: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ) = flow {
        logDebug("enterToAccount", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            authorizationApi.enterToAccount(
                pin = hashedPin,
                fcm = firebaseToken,
                clientId = CLIENT_ID,
                scope = CLIENT_SCOPE,
                grantType = GRANT_TYPE_PASSWORD,
                egn = personalIdentificationNumber,
            )
        }.onSuccess {
            logDebug("enterToAccount onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onRetry {
            logDebug("enterToAccount onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("enterToAccount onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

    override fun refreshAccessToken(
        refreshToken: String
    ) = flow {
        logDebug("refreshAccessToken", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            authorizationApi.refreshAccessToken(
                clientId = CLIENT_ID,
                scope = CLIENT_SCOPE,
                grantType = GRANT_TYPE_REFRESH_TOKEN,
                refreshToken = refreshToken,
            )
        }.onSuccess {
            logDebug("refreshAccessToken onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onRetry {
            logDebug("refreshAccessToken onRetry", TAG)
            emit(ResultEmittedData.retry(null))
        }.onFailure {
            logError("refreshAccessToken onFailure", it, TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(Dispatchers.IO)

}