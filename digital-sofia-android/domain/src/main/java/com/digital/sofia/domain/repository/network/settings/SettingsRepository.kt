/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.network.settings

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.settings.ChangePinRequestModel
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun changePin(
        data: ChangePinRequestModel,
    ): Flow<ResultEmittedData<Unit>>

    fun deleteUser(): Flow<ResultEmittedData<Unit>>

}