/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.mappers.documents

import com.digital.sofia.R
import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.domain.models.documents.DocumentModel
import com.digital.sofia.models.documents.DocumentHeaderUi
import com.digital.sofia.models.documents.DocumentUi
import com.digital.sofia.models.documents.DocumentsAdapterMarker
import com.digital.sofia.utils.CurrentContext

class DocumentsUiMapper(
    private val currentContext: CurrentContext,
) : BaseMapper<List<DocumentModel>, List<DocumentsAdapterMarker>>() {

    fun mapWithHeader(from: List<DocumentModel>): List<DocumentsAdapterMarker> {
        return buildList {
            add(DocumentHeaderUi())
            addAll(map(from))
        }
    }

    override fun map(from: List<DocumentModel>): List<DocumentsAdapterMarker> {
        return buildList{
            addAll(from.map(::mapItem))
        }
    }

    private fun mapItem(from: DocumentModel): DocumentsAdapterMarker {
        return with(from) {
            DocumentUi(
                status = status,
                type = type ?: currentContext.getString(R.string.unknown),
                signed = signed,
                created = created ?: currentContext.getString(R.string.unknown),
                expired = expired,
                rejected = rejected,
                generated = generated,
                fileUrl = fileUrl ?: currentContext.getString(R.string.unknown),
                fileName = fileName ?: currentContext.getString(R.string.unknown),
                formioId = formioId ?: currentContext.getString(R.string.unknown),
                applicationId = applicationId ?: currentContext.getString(R.string.unknown),
                evrotrustTransactionId = evrotrustTransactionId,
            )
        }
    }

}