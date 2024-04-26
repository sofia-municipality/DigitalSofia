/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.authorization

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.token.AccessTokenModel
import com.digital.sofia.domain.models.token.RefreshTokenModel
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import java.util.Date

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class AuthorizationEnterToAccountUseCase(
    private val preferences: PreferencesRepository,
    private val authorizationHelper: AuthorizationHelper,
    private val authorizationNetworkRepository: AuthorizationNetworkRepository,
) {

    companion object {
        private const val TAG = "AuthorizationEnterToAccountUseCaseTag"
    }

    fun invoke(
        hashedPin: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("enterToAccount", TAG)
        authorizationNetworkRepository.enterToAccount(
            hashedPin = hashedPin,
            firebaseToken = firebaseToken,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("enterToAccount onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("enterToAccount onSuccess", TAG)
                val currentTime = Date().time
                if (!it.accessToken.isNullOrEmpty() && it.expiresIn != null && it.expiresIn != 0L) {
                    logDebug("enterToAccount save accessToken", TAG)
                    val accessTokenModel = AccessTokenModel(
                        token = it.accessToken,
                        expirationTime = currentTime + it.expiresIn * 1000L,
                    )
                    preferences.saveAccessToken(
                        value = accessTokenModel
                    )
                    logDebug("enterToAccount startUpdateAccessTokenTimer", TAG)
                    authorizationHelper.startUpdateAccessTokenTimer(accessTokenModel.expirationTime)
                }
                if (!it.refreshToken.isNullOrEmpty() && it.refreshExpiresIn != null && it.refreshExpiresIn != 0L) {
                    logDebug("enterToAccount save refreshToken", TAG)
                    val refreshTokenModel = RefreshTokenModel(
                        token = it.refreshToken,
                        expirationTime = currentTime + it.refreshExpiresIn * 1000L,
                    )
                    preferences.saveRefreshToken(
                        value = refreshTokenModel
                    )
                }
                emit(ResultEmittedData.success(Unit))
            }.onRetry {
                logDebug("enterToAccount onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("enterToAccount onFailure", it, TAG)
                emit(ResultEmittedData.error(it, null))
            }
        }.collect()
    }.flowOn(Dispatchers.IO)

}