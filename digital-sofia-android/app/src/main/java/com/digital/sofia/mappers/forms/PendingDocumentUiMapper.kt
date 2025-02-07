/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.mappers.forms

import com.digital.sofia.R
import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.models.forms.PendingDocumentUi
import com.digital.sofia.utils.CurrentContext

class PendingDocumentUiMapper(
    private val currentContext: CurrentContext,
) : BaseMapper<DocumentModel, PendingDocumentUi>() {

    override fun map(from: DocumentModel): PendingDocumentUi {
        return with(from) {
            PendingDocumentUi(
                evrotrustTransactionId = evrotrustTransactionId,
                evrotrustThreadId = evrotrustThreadId,
                status = status,
                title = fileName ?: currentContext.getString(R.string.unknown),
            )
        }
    }

}