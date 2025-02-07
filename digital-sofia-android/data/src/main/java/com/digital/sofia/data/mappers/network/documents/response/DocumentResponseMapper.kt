package com.digital.sofia.data.mappers.network.documents.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.documents.DocumentResponse
import com.digital.sofia.domain.extensions.getEnumTypeValue
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.domain.models.documents.DocumentStatusModel

class DocumentResponseMapper: BaseMapper<DocumentResponse, DocumentModel>() {
    override fun map(from: DocumentResponse): DocumentModel {
        return with(from) {
            val documentStatus = if (status != null) {
                getEnumTypeValue<DocumentStatusModel>(status) ?: DocumentStatusModel.UNKNOWN
            } else DocumentStatusModel.UNKNOWN

            DocumentModel(
                applicationId = applicationId,
                status = documentStatus,
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
                evrotrustTransactionId = evrotrustTransactionId ?: "",
                formioId = formioId,
                fileUrl = fileUrl,
                referenceNumber = referenceNumber,
            )
        }
    }
}