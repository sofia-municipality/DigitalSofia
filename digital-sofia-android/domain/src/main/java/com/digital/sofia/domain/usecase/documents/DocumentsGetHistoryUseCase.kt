package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class DocumentsGetHistoryUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsGetHistoryUseCaseTag"
        private val STATUSES = listOf(
            DocumentStatusModel.EXPIRED,
            DocumentStatusModel.REJECTED,
            DocumentStatusModel.SIGNED,
            DocumentStatusModel.UNSIGNED,
            DocumentStatusModel.FAILED,
            DocumentStatusModel.WITHDRAWN,
        )
    }

    fun invoke(cursor: String?): Flow<ResultEmittedData<DocumentsWithPaginationModel>> {
        logDebug("getDocumentsHistory cursor: $cursor", TAG)
        val statuses = STATUSES.joinToString(separator = ",") { it.type }
        return documentsNetworkRepository.getDocuments(status = statuses, cursor = cursor)
    }

}