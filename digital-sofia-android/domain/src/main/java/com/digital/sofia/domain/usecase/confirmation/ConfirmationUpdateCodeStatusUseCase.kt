/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.confirmation

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusModel
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusRequestModel
import com.digital.sofia.domain.repository.network.confirmation.ConfirmationNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class ConfirmationUpdateCodeStatusUseCase(
    private val confirmationNetworkRepository: ConfirmationNetworkRepository,
) {

    companion object {
        private const val TAG = "ConfirmationUpdateCodeStatusUseCaseTag"
    }

    fun updateCodeStatus(
        code: String,
        status: String,
    ): Flow<ResultEmittedData<ConfirmationUpdateCodeStatusModel>> {
        logDebug("updateCodeStatus code: $code status: $status", TAG)
        return confirmationNetworkRepository.updateCodeStatus(
            data = ConfirmationUpdateCodeStatusRequestModel(code = code, status = status),
        )
    }

}