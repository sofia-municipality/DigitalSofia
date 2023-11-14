package com.digitall.digital_sofia.domain.usecase.registration

import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digitall.digital_sofia.domain.models.registration.CheckPinModel
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface RegistrationUseCase {

    fun registerNewUser(
        email: String,
        hashedPin: String,
        phoneNumber: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<Unit>>

    fun checkUser(personalIdentificationNumber: String):
            Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>>

    fun checkPin(
        hashedPin: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<CheckPinModel>>

    fun sendSignedDocument(
        refreshToken: String,
        evrotrustTransactionId: String,
    ): Flow<ResultEmittedData<Unit>>

}

class RegistrationUseCaseImpl(
    private val preferences: PreferencesRepository,
    private val registrationNetworkRepository: RegistrationNetworkRepository,
) : RegistrationUseCase {

    companion object {
        private const val TAG = "RegistrationUseCaseTag"
    }

    override fun registerNewUser(
        email: String,
        hashedPin: String,
        phoneNumber: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("registerNewUser", TAG)
        registrationNetworkRepository.registerNewUser(
            email = email,
            hashedPin = hashedPin,
            phoneNumber = phoneNumber,
            firebaseToken = firebaseToken,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("registerNewUser onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("registerNewUser onSuccess", TAG)
                if (it.accessToken.isNotEmpty()) {
                    preferences.saveAccessToken(it.accessToken)
                }
                if (it.refreshToken.isNotEmpty()) {
                    preferences.saveRefreshToken(it.refreshToken)
                }
                emit(ResultEmittedData.success(Unit))
            }.onFailure { documentError ->
                logError("registerNewUser onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

    override fun checkUser(personalIdentificationNumber: String):
            Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>> {
        logDebug("checkUser", TAG)
        return registrationNetworkRepository.checkUser(
            personalIdentificationNumber = personalIdentificationNumber,
        )
    }

    override fun checkPin(
        hashedPin: String,
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPinModel>> {
        logDebug("checkPin", TAG)
        return registrationNetworkRepository.checkPin(
            hashedPin = hashedPin,
            personalIdentificationNumber = personalIdentificationNumber,
        )
    }

    override fun sendSignedDocument(
        refreshToken: String,
        evrotrustTransactionId: String,
    ): Flow<ResultEmittedData<Unit>> = flow {
        logDebug("sendSignedDocument", TAG)
        registrationNetworkRepository.sendSignedDocument(
            refreshToken = refreshToken,
            evrotrustTransactionId = evrotrustTransactionId,
        ).onEach { result ->
            result.onLoading {
                logDebug("sendSignedDocument onLoading", TAG)
                emit(ResultEmittedData.loading(null))
            }.onSuccess {
                logDebug("sendSignedDocument onSuccess", TAG)
                if (it.accessToken.isNotEmpty()) {
                    preferences.saveAccessToken(it.accessToken)
                }
                if (it.refreshToken.isNotEmpty()) {
                    preferences.saveRefreshToken(it.refreshToken)
                }
                emit(ResultEmittedData.success(Unit))
            }.onFailure { documentError ->
                logError("sendSignedDocument onFailure", TAG)
                emit(ResultEmittedData.error(documentError, null))
            }
        }.collect()
    }

}