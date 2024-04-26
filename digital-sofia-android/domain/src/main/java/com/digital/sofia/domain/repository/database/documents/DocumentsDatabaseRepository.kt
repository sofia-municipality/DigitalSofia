/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.database.documents

import com.digital.sofia.domain.models.documents.DocumentModel
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface DocumentsDatabaseRepository : Serializable {

    fun saveDocuments(list: List<DocumentModel>)

    fun subscribeToDocuments(): Flow<List<DocumentModel>>

    fun subscribeToDocumentsWithStatus(status: List<String>): Flow<List<DocumentModel>>

    fun haveDocumentsWithStatus(status: List<String>): Boolean

    fun clear()

}