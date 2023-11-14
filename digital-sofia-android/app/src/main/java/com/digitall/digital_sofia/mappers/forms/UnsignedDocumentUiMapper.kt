package com.digitall.digital_sofia.mappers.forms

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.domain.models.common.TEXT_UNKNOWN
import com.digitall.digital_sofia.domain.models.documents.DocumentModel
import com.digitall.digital_sofia.models.forms.UnsignedDocumentUi

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class UnsignedDocumentUiMapper : BaseMapper<DocumentModel, UnsignedDocumentUi>() {

    override fun map(from: DocumentModel): UnsignedDocumentUi {
        return with(from) {
            UnsignedDocumentUi(
                evrotrustTransactionId = evrotrustTransactionId,
                title = fileName ?: TEXT_UNKNOWN
            )
        }
    }

}