/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.network.registration

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digital.sofia.domain.models.user.RegisterNewUserRequestModel
import kotlinx.coroutines.flow.Flow

interface RegistrationNetworkRepository {

    fun registerNewUser(
        data: RegisterNewUserRequestModel
    ): Flow<ResultEmittedData<Unit>>

    fun checkUser(
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>>

}