/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.database.documents

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val generated: String?,
    val modified: String?,
    val fileName: String?,
    val validUntill: String?,
    val applicationId: String?,
    val referenceNumber: String?,
    val evrotrustThreadId: String?,
)
