package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.documents.DocumentAuthenticationRequestModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class DocumentsAuthenticateDocumentUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository
) {

    companion object {
        private const val TAG = "DocumentsAuthenticateDocumentUseCaseTag"
    }

    fun invoke(
        pin: String,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        evrotrustTransactionId: String,
    ): Flow<ResultEmittedData<Unit>> {
        logDebug(
            "authenticateDocument pin: $pin email: $email phoneNumber: $phoneNumber firebaseToken $firebaseToken evrotrustTransactionId: $evrotrustTransactionId",
            TAG
        )
        return documentsNetworkRepository.authenticateDocument(
            data = DocumentAuthenticationRequestModel(
                pin = pin,
                email = email,
                phoneNumber = phoneNumber,
                firebaseToken = firebaseToken
            ),
            evrotrustTransactionId = evrotrustTransactionId,
        )
    }
}