package com.digitall.digital_sofia.domain.usecase.authorization

import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface AuthorizationUseCase {

    fun enterToAccount(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<Unit>>

    fun getAccessToken(
        refreshToken: String,
    ): Flow<ResultEmittedData<Unit>>

}

class AuthorizationUseCaseImpl(
    private val preferences: PreferencesRepository,
    private val authorizationNetworkRepository: AuthorizationNetworkRepository,
) : AuthorizationUseCase {

    companion object {
        private const val TAG = "AuthorizationUseCaseTag"
    }

    override fun enterToAccount(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("enterToAccount", TAG)
        authorizationNetworkRepository.enterToAccount(
            hashedPin = hashedPin,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("enterToAccount onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("enterToAccount onSuccess", TAG)
                if (it.accessToken.isNotEmpty()) {
                    preferences.saveAccessToken(it.accessToken)
                }
                if (it.refreshToken.isNotEmpty()) {
                    preferences.saveRefreshToken(it.refreshToken)
                }
                emit(ResultEmittedData.success(Unit))
            }.onFailure { documentError ->
                logError("enterToAccount onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

    override fun getAccessToken(
        refreshToken: String,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("getAccessToken", TAG)
        authorizationNetworkRepository.getAccessToken(
            refreshToken = refreshToken,
        ).onEach { result ->
            result.onLoading {
                logDebug("getAccessToken onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("getAccessToken onSuccess", TAG)
                if (it.accessToken.isNotEmpty()) {
                    preferences.saveAccessToken(it.accessToken)
                }
                if (it.refreshToken.isNotEmpty()) {
                    preferences.saveRefreshToken(it.refreshToken)
                }
                emit(ResultEmittedData.success(Unit))
            }.onFailure { documentError ->
                logError("getAccessToken onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

}