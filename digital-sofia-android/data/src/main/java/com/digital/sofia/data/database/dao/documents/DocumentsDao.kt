/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.database.dao.documents

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.digital.sofia.data.models.database.documents.DocumentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentsDao {

    @Query("SELECT * FROM documents")
    fun subscribeToDocuments(): Flow<List<DocumentsEntity>>

    @Query("SELECT * FROM documents WHERE status IN (:status)")
    fun subscribeToDocumentsWithStatus(status: List<String>): Flow<List<DocumentsEntity>>

    @Query("SELECT EXISTS(SELECT * FROM documents WHERE status IN (:status))")
    fun haveDocumentsWithStatus(status: List<String>): Boolean

    @Transaction
    fun replaceDocuments(list: List<DocumentsEntity>) {
        deleteDocuments()
        saveDocuments(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDocuments(list: List<DocumentsEntity>)

    @Query("DELETE FROM documents")
    fun deleteDocuments()
}