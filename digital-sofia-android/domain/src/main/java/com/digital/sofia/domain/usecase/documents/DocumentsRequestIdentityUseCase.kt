package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class DocumentsRequestIdentityUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository
) {

    companion object {
        private const val TAG = "DocumentsRequestIdentityUseCaseTag"
    }

    fun invoke(
        personalIdentificationNumber: String,
        language: String,
    ): Flow<ResultEmittedData<DocumentModel>> {
        logDebug(
            "requestIdentity personalIdentificationNumber: $personalIdentificationNumber",
            TAG
        )
        return documentsNetworkRepository.requestIdentity(
            personalIdentificationNumber = personalIdentificationNumber,
            language = language,
        )
    }
}