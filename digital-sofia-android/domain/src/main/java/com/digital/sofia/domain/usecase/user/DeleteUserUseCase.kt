package com.digital.sofia.domain.usecase.user

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.repository.network.user.UserRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class DeleteUserUseCase(
    private val preferences: PreferencesRepository,
    private val userRepository: UserRepository,
    private val authorizationHelper: AuthorizationHelper,
) {

    companion object {
        private const val TAG = "DeleteUserUseCaseTag"
    }

    fun invoke(): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("deleteUser", TAG)
        emit(ResultEmittedData.loading(null))
        userRepository.deleteUser().onEach { result ->
            result.onLoading {
                logDebug("logout onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("logout onSuccess", TAG)
                authorizationHelper.stopUpdateTokenTimer()
                preferences.logoutFromPreferences()
                emit(ResultEmittedData.success(Unit))
            }.onRetry {
                logDebug("logout onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("logout onFailure", it, TAG)
                emit(ResultEmittedData.error(it, null))
            }
        }.collect()
    }.flowOn(Dispatchers.IO)

}