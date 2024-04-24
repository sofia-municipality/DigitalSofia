/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.logout

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class LogoutUseCase(
    private val preferences: PreferencesRepository,
    private val settingsRepository: SettingsRepository,
    private val authorizationHelper: AuthorizationHelper,
    private val documentsDatabaseRepository: DocumentsDatabaseRepository,
) {

    companion object {
        private const val TAG = "LogoutUseCaseTag"
    }

    fun invoke(force: Boolean): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("logout", TAG)
        emit(ResultEmittedData.loading(null))
        settingsRepository.deleteUser().onEach { result ->
            result.onLoading {
                logDebug("logout onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("logout onSuccess", TAG)
                authorizationHelper.stopUpdateTokenTimer()
                preferences.logoutFromPreferences()
                documentsDatabaseRepository.clear()
                emit(ResultEmittedData.success(Unit))
            }.onRetry {
                logDebug("logout onRetry", TAG)
                emit(ResultEmittedData.retry(null))
            }.onFailure {
                logError("logout onFailure", it, TAG)
                if (force) {
                    authorizationHelper.stopUpdateTokenTimer()
                    preferences.logoutFromPreferences()
                    documentsDatabaseRepository.clear()
                }
                emit(ResultEmittedData.error(it, null))
            }
        }.collect()
    }.flowOn(Dispatchers.IO)

}