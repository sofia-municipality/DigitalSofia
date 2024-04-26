/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class DocumentsHaveUnsignedUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsSubscribeToUnsignedUseCaseTag"
    }

    fun invoke(status: String): Flow<ResultEmittedData<DocumentsWithPaginationModel>> {
        logDebug("haveUnsignedDocuments", TAG)
        return documentsNetworkRepository.getDocuments(status = status)
    }

}