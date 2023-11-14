package com.digitall.digital_sofia.data.mappers.network.documents

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.data.models.network.documents.DocumentsResponse
import com.digitall.digital_sofia.domain.extensions.getEnumTypeValue
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.domain.models.documents.DocumentStatusModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsResponseMapper : BaseMapper<DocumentsResponse, List<DocumentModel>>() {

    override fun map(from: DocumentsResponse): List<DocumentModel> {
        return buildList {
            from.documents?.forEach { document ->
                if (document.evrotrustTransactionId.isNullOrEmpty()) return@forEach
                add(
                    DocumentModel(
                        applicationId = document.applicationId,
                        status = document.status?.let {
                            getEnumTypeValue<DocumentStatusModel>(it)
                                ?: DocumentStatusModel.UNKNOWN
                        } ?: DocumentStatusModel.UNKNOWN,
                        type = document.type,
                        fileName = document.fileName,
                        created = document.created,
                        modified = document.modified,
                        signed = document.signed,
                        expired = document.expired,
                        validUntill = document.validUntill,
                        rejected = document.rejected,
                        evrotrustThreadId = document.evrotrustThreadId,
                        evrotrustTransactionId = document.evrotrustTransactionId,
                        formioId = document.formioId,
                        fileUrl = document.fileUrl,
                        referenceNumber = document.referenceNumber,
                    )
                )
            }
        }
    }
}