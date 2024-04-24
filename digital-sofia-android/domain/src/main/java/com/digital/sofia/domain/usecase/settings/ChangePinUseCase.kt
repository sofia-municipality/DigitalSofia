/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.settings

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.settings.ChangePinRequestModel
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class ChangePinUseCase(
    private val settingsRepository: SettingsRepository,
) {

    companion object {
        private const val TAG = "ChangePinUseCaseTag"
    }

    fun invoke(
        newPin: String,
    ): Flow<ResultEmittedData<Unit>> {
        logDebug("changePin newPin: $newPin", TAG)
        return settingsRepository.changePin(
            data = ChangePinRequestModel(pin = newPin),
        )
    }

}