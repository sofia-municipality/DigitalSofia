package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.documents.DocumentStatusModel
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

class DocumentsGetPendingUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsGetUnsignedUseCaseTag"
        private val STATUSES = listOf(
            DocumentStatusModel.SIGNING,
            DocumentStatusModel.DELIVERING
        )
    }

    fun invoke(cursor: String?): Flow<ResultEmittedData<DocumentsWithPaginationModel>> {
        logDebug("getUnsignedDocuments cursor: $cursor", TAG)
        val statuses = STATUSES.joinToString(separator = ",") { status -> status.type }
        return documentsNetworkRepository.getDocuments(status = statuses, cursor = cursor)
    }

}