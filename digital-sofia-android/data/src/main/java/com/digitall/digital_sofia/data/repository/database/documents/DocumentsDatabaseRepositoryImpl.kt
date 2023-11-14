package com.digitall.digital_sofia.data.repository.database.documents

import com.digitall.digital_sofia.data.database.dao.documents.DocumentsDao
import com.digitall.digital_sofia.data.mappers.database.documents.DocumentsEntityMapper
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import com.digitall.digital_sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsDatabaseRepositoryImpl(
    private val dao: DocumentsDao,
    private val mapper: DocumentsEntityMapper,
) : DocumentsDatabaseRepository {

    companion object {
        private const val TAG = "DocumentsDatabaseRepositoryImplTag"
    }

    override fun saveDocuments(list: List<DocumentModel>) {
        logDebug("saveDocuments size: ${list.size}", TAG)
        dao.replaceDocuments(mapper.reverseList(list))
    }

    override fun subscribeToDocuments(): Flow<List<DocumentModel>> {
        val documents = dao.subscribeToDocuments().map(mapper::mapList)
        logDebug("subscribeToDocuments", TAG)
        return documents
    }

    override fun subscribeToDocumentsWithStatus(status: DocumentStatusModel): Flow<List<DocumentModel>> {
        val documents = dao.subscribeToDocumentsWithStatus(status.type).map(mapper::mapList)
        logDebug("subscribeToDocumentsWithStatus status: ${status.type}", TAG)
        return documents
    }

    override fun clear() {
        dao.deleteDocuments()
    }


}