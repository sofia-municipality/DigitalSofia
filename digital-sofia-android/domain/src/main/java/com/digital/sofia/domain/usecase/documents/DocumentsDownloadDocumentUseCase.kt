/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.base.ResultEmittedData
import com.digital.sofia.domain.models.common.DownloadProgress
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow
import java.io.File

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class DocumentsDownloadDocumentUseCase(
    private val documentsNetworkRepository: DocumentsNetworkRepository,
) {

    companion object {
        private const val TAG = "DocumentsDownloadDocumentUseCaseTag"
    }

    fun invoke(
        file: File,
        documentUrl: String,
    ): Flow<ResultEmittedData<DownloadProgress>> {
        logDebug("downloadDocument documentUrl: $documentUrl", TAG)
        return documentsNetworkRepository.downloadFile(
            file = file,
            documentUrl = documentUrl,
        )
    }

}