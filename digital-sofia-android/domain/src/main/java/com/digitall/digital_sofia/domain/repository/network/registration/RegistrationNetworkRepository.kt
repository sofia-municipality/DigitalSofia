package com.digitall.digital_sofia.domain.repository.network.registration

import com.digitall.digital_sofia.domain.models.authorization.AuthorizationModel
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digitall.digital_sofia.domain.models.registration.CheckPinModel
import kotlinx.coroutines.flow.Flow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface RegistrationNetworkRepository {

    fun registerNewUser(
        email: String,
        hashedPin: String,
        phoneNumber: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

    fun checkUser(
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>>

    fun checkPin(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<CheckPinModel>>

    fun sendSignedDocument(
        evrotrustTransactionId: String,
        refreshToken: String,
    ): Flow<ResultEmittedData<AuthorizationModel>>

}