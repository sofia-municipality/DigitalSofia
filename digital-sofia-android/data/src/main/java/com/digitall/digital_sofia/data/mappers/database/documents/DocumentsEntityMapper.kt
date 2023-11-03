package com.digitall.digital_sofia.data.mappers.database.documents

import com.digitall.digital_sofia.data.mappers.base.BaseReverseMapper
import com.digitall.digital_sofia.data.models.database.documents.DocumentsEntity
import com.digitall.digital_sofia.domain.extensions.getEnumTypeValue
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsEntityMapper : BaseReverseMapper<DocumentsEntity, DocumentModel>() {

    override fun map(from: DocumentsEntity): DocumentModel {
        return with(from) {
            DocumentModel(
                applicationId = applicationId,
                status = status.let {
                    getEnumTypeValue<DocumentStatusModel>(it)
                        ?: DocumentStatusModel.UNKNOWN
                },
                type = type,
                fileName = fileName,
                created = created,
                modified = modified,
                signed = signed,
                expired = expired,
                validUntill = validUntill,
                rejected = rejected,
                evrotrustThreadId = evrotrustThreadId,
                evrotrustTransactionId = evrotrustTransactionId,
                formioId = formioId,
                referenceNumber = referenceNumber,
                fileUrl = fileUrl,
            )
        }
    }


    override fun reverse(to: DocumentModel): DocumentsEntity {
        return with(to) {
            DocumentsEntity(
                applicationId = applicationId,
                status = status.type,
                type = type,
                fileName = fileName,
                created = created,
                modified = modified,
                signed = signed,
                expired = expired,
                validUntill = validUntill,
                rejected = rejected,
                evrotrustThreadId = evrotrustThreadId,
                evrotrustTransactionId = evrotrustTransactionId,
                formioId = formioId,
                referenceNumber = referenceNumber,
                fileUrl = fileUrl,
            )
        }
    }

}