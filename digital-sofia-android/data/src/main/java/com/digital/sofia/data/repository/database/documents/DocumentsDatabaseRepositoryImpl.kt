/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.repository.database.documents

import com.digital.sofia.data.database.dao.documents.DocumentsDao
import com.digital.sofia.data.mappers.database.documents.DocumentsEntityMapper
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
        logDebug("subscribeToDocuments", TAG)
        return dao.subscribeToDocuments()
            .map(mapper::mapList)
            .flowOn(Dispatchers.IO)
    }

    override fun subscribeToDocumentsWithStatus(status: List<String>): Flow<List<DocumentModel>> {
        return dao.subscribeToDocumentsWithStatus(status)
            .map(mapper::mapList)
            .flowOn(Dispatchers.IO)
    }

    override fun haveDocumentsWithStatus(status: List<String>): Boolean {
        return dao.haveDocumentsWithStatus(status)
    }

    override fun clear() {
        dao.deleteDocuments()
    }


}