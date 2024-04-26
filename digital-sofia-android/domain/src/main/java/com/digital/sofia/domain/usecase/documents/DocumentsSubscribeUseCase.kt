/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.usecase.documents

import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow

/** imports for flow:
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.flow
 * import kotlinx.coroutines.flow.onEach
 **/

class DocumentsSubscribeUseCase(
    private val documentsDatabaseRepository: DocumentsDatabaseRepository,
) {

    companion object {
        private const val TAG = "DocumentsSubscribeUseCaseTag"
    }

    fun invoke(): Flow<List<DocumentModel>> {
        logDebug("subscribeToDocuments", TAG)
        return documentsDatabaseRepository.subscribeToDocuments()
    }

}