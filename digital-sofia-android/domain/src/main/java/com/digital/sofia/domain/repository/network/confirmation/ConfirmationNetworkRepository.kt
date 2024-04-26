/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.network.confirmation

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.confirmation.ConfirmationCodeStatusModel
import com.digital.sofia.domain.models.confirmation.ConfirmationGenerateCodeModel
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusModel
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusRequestModel
import kotlinx.coroutines.flow.Flow

interface ConfirmationNetworkRepository {

    fun generateCode(
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<ConfirmationGenerateCodeModel>>

    fun getCodeStatus(): Flow<ResultEmittedData<ConfirmationCodeStatusModel>>

    fun updateCodeStatus(
        data: ConfirmationUpdateCodeStatusRequestModel,
    ): Flow<ResultEmittedData<ConfirmationUpdateCodeStatusModel>>

}