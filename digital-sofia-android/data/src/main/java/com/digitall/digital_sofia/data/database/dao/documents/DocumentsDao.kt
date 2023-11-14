package com.digitall.digital_sofia.data.database.dao.documents

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.digitall.digital_sofia.data.models.database.documents.DocumentsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Dao
interface DocumentsDao {

    @Query("SELECT * FROM documents")
    fun subscribeToDocuments(): Flow<List<DocumentsEntity>>

    @Query("SELECT * FROM documents WHERE status = :status")
    fun subscribeToDocumentsWithStatus(status: String): Flow<List<DocumentsEntity>>

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