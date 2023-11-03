package com.digitall.digital_sofia.mappers.documents

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.domain.models.common.TEXT_UNKNOWN
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.models.documents.DocumentUi

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class DocumentsUiMapper : BaseMapper<DocumentModel, DocumentUi>() {

    override fun map(from: DocumentModel): DocumentUi {
        return with(from) {
            DocumentUi(
                status = status,
                type = type ?: TEXT_UNKNOWN,
                signed = signed ?: TEXT_UNKNOWN,
                created = created ?: TEXT_UNKNOWN,
                expired = expired ?: TEXT_UNKNOWN,
                fileUrl = fileUrl ?: TEXT_UNKNOWN,
                fileName = fileName ?: TEXT_UNKNOWN,
                formioId = formioId ?: TEXT_UNKNOWN,
                applicationId = applicationId ?: TEXT_UNKNOWN,
                evrotrustTransactionId = evrotrustTransactionId,
            )
        }
    }

}