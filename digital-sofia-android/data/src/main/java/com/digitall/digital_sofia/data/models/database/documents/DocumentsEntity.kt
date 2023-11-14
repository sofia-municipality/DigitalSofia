package com.digitall.digital_sofia.data.models.database.documents

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Entity(tableName = "documents")
data class DocumentsEntity(
    @PrimaryKey val evrotrustTransactionId: String,
    val type: String?,
    val status: String,
    val signed: String?,
    val expired: String?,
    val fileUrl: String?,
    val created: String?,
    val formioId: String?,
    val rejected: String?,
    val modified: String?,
    val fileName: String?,
    val validUntill: String?,
    val applicationId: String?,
    val referenceNumber: String?,
    val evrotrustThreadId: String?,
)

//data class DocumentsEntity(
//    @Embedded val document: DocumentEntity,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "documentId",
//        entity = DocumentFileEntity::class
//    )
//    val file: List<DocumentFileEntity>
//)
//
//@Entity(tableName = "documents")
//data class DocumentEntity(
//    @PrimaryKey val id: String,
//    val created: String,
//    val modified: String,
//    val userId: String,
//    val applicationId: String,
//    val status: String,
//    val evrotrustThreadId: String,
//    val evrotrustTransactionId: String,
//)
//
//@Entity(tableName = "documentsFiles")
//data class DocumentFileEntity(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0,
//    val documentId: String,
//    val name: String,
//    val originalName: String,
//    val size: Int,
//    val storage: String,
//    val type: String,
//)
