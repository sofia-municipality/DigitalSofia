package com.digitall.digital_sofia.domain.repository.database.documents

import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface DocumentsDatabaseRepository : Serializable {

    fun saveDocuments(list: List<DocumentModel>)

    fun subscribeToDocuments(): Flow<List<DocumentModel>>

    fun subscribeToDocumentsWithStatus(status: DocumentStatusModel): Flow<List<DocumentModel>>

    fun clear()

}