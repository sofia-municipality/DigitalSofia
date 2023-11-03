package com.digitall.digital_sofia.data.repository.network.registration

import com.digitall.digital_sofia.data.CLIENT_ID
import com.digitall.digital_sofia.data.CLIENT_SCOPE
import com.digitall.digital_sofia.data.GRANT_TYPE
import com.digitall.digital_sofia.data.mappers.network.authorization.AuthorizationResponseMapper
import com.digitall.digital_sofia.data.mappers.network.registration.CheckPersonalIdentificationNumberResponseMapper
import com.digitall.digital_sofia.data.mappers.network.registration.CheckPinResponseMapper
import com.digitall.digital_sofia.data.network.documents.DocumentsApi
import com.digitall.digital_sofia.data.network.registration.RegistrationApi
import com.digitall.digital_sofia.data.repository.network.base.BaseRepository
import com.digitall.digital_sofia.domain.models.authorization.AuthorizationModel
import com.digitall.digital_sofia.domain.models.base.ResultEmittedData
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.registration.CheckPersonalIdentificationNumberModel
import com.digitall.digital_sofia.domain.models.registration.CheckPinModel
import com.digitall.digital_sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class RegistrationNetworkRepositoryImpl(
    private val documentsApi: DocumentsApi,
    private val registrationApi: RegistrationApi,
    private val dispatcherIO: CoroutineDispatcher,
    private val checkPinResponseMapper: CheckPinResponseMapper,
    private val authorizationResponseMapper: AuthorizationResponseMapper,
    private val checkPersonalIdentificationNumberResponseMapper: CheckPersonalIdentificationNumberResponseMapper,
) : RegistrationNetworkRepository, BaseRepository() {

    companion object {
        private const val TAG = "RegistrationRepositoryTag"
    }

    override fun registerNewUser(
        email: String,
        hashedPin: String,
        phoneNumber: String,
        firebaseToken: String,
        personalIdentificationNumber: String,
    ): Flow<ResultEmittedData<AuthorizationModel>> = flow {
        logDebug("registerNewUser", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            registrationApi.registerNewUser(
                email = email,
                pin = hashedPin,
                fcm = firebaseToken,
                clientId = CLIENT_ID,
                scope = CLIENT_SCOPE,
                grantType = GRANT_TYPE,
                phoneNumber = phoneNumber,
                egn = personalIdentificationNumber,
            )
        }.onSuccess {
            logDebug("registerNewUser onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onFailure {
            logError("registerNewUser onFailure error code: ${it.responseCode}", TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

    override fun checkUser(
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPersonalIdentificationNumberModel>> = flow {
        logDebug("checkUser", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            registrationApi.checkUser(personalIdentificationNumber)
        }.onSuccess {
            val result = checkPersonalIdentificationNumberResponseMapper.map(it)
            logDebug("checkUser onSuccess", TAG)
            emit(ResultEmittedData.success(result))
        }.onFailure {
            logError(
                "checkUser onFailure error code: ${it.responseCode}",
                TAG
            )
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

    override fun checkPin(
        hashedPin: String,
        personalIdentificationNumber: String
    ): Flow<ResultEmittedData<CheckPinModel>> = flow {
        logDebug("checkPin", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            registrationApi.checkPin(
                pin = hashedPin,
                personalIdentificationNumber = personalIdentificationNumber,
            )
        }.onSuccess {
            logDebug("checkPin onSuccess", TAG)
            emit(ResultEmittedData.success(checkPinResponseMapper.map(it)))
        }.onFailure {
            logError(
                "checkPin onFailure error code: ${it.responseCode}",
                TAG
            )
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

    override fun sendSignedDocument(
        evrotrustTransactionId: String,
        refreshToken: String
    ): Flow<ResultEmittedData<AuthorizationModel>> = flow {
        logDebug("sendSignedDocument", TAG)
        emit(ResultEmittedData.loading(null))
        getResult {
            documentsApi.sendSignedDocument(
                evrotrustTransactionId = evrotrustTransactionId,
                refreshToken = refreshToken,
            )
        }.onSuccess {
            logDebug("sendSignedDocument onSuccess", TAG)
            emit(ResultEmittedData.success(authorizationResponseMapper.map(it)))
        }.onFailure {
            logError("sendSignedDocument onFailure error code: ${it.responseCode}", TAG)
            emit(ResultEmittedData.error(it, null))
        }
    }.flowOn(dispatcherIO)

}