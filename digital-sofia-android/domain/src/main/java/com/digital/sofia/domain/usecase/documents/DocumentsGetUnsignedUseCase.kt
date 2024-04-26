package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class DocumentsGetUnsignedUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsGetUnsignedUseCaseTag"
        private const val SIGNING_STATUS = "signing"
    }

    fun invoke(cursor: String?): Flow<ResultEmittedData<DocumentsWithPaginationModel>> {
        logDebug("getUnsignedDocuments cursor: $cursor", TAG)
        return documentsNetworkRepository.getDocuments(status = SIGNING_STATUS, cursor = cursor)
    }

}