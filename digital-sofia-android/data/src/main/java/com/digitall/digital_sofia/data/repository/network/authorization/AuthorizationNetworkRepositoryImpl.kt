package com.digitall.digital_sofia.data.repository.network.authorization

import com.digitall.digital_sofia.data.CLIENT_ID
import com.digitall.digital_sofia.data.CLIENT_SCOPE
import com.digitall.digital_sofia.data.GRANT_TYPE
import com.digitall.digital_sofia.data.mappers.network.authorization.AuthorizationResponseMapper
import com.digitall.digital_sofia.data.network.authorization.AuthorizationApi
import com.digitall.digital_sofia.data.repository.network.base.BaseRepository
import com.digitall.digital_sofia.domain.models.authorization.AuthorizationModel
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthorizationNetworkRepositoryImpl(
    private val api: AuthorizationApi,
    private val dispatcherIO: CoroutineDispatcher,
    private val authorizationResponseMapper: AuthorizationResponseMapper,
) : AuthorizationNetworkRepository, BaseRepository() {

    companion object {
        private const val TAG = "AuthorizationRepositoryTag"
    }

    override fun enterToAccount(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<AuthorizationModel>> = flow {
        logDebug("enterToAccount", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            api.enterToAccount(
                grantType = GRANT_TYPE,
                clientId = CLIENT_ID,
                scope = CLIENT_SCOPE,
                pin = hashedPin,
                egn = personalIdentificationNumber,
            )
        }.onSuccess {
            logDebug("enterToAccount onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onFailure {
            logError("enterToAccount onFailure error code: ${it.responseCode}", TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

    override fun getAccessToken(
        refreshToken: String,
    ): Flow<ResultEmittedData<AuthorizationModel>> = flow {
        logDebug("getAccessToken", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            api.getAccessTokenWithRefreshToken(
                clientId = CLIENT_ID,
                scope = CLIENT_SCOPE,
                grantType = GRANT_TYPE,
                refreshToken = refreshToken,
            )
        }.onSuccess {
            logDebug("getAccessToken onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onFailure {
            logError(
                "getAccessToken onFailure error code: ${it.responseCode}",
                TAG
            )
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

}