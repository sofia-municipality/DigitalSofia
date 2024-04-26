/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.registration

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digital.sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class RegistrationCheckUserUseCase(
    private val registrationNetworkRepository: RegistrationNetworkRepository,
) {

    companion object {
        private const val TAG = "CheckUserUseCaseTag"
    }

    fun invoke(
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>> {
        logDebug("checkUser personalIdentificationNumber: $personalIdentificationNumber", TAG)
        return registrationNetworkRepository.checkUser(
            personalIdentificationNumber = personalIdentificationNumber,
        )
    }

}