/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.database.documents

import com.digital.sofia.data.mappers.base.BaseReverseMapper
import com.digital.sofia.data.models.database.documents.DocumentsEntity
import com.digital.sofia.domain.extensions.getEnumTypeValue
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.models.documents.DocumentStatusModel

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
                generated = generated,
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
                generated = generated,
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