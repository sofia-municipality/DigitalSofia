/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.network.documents.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.documents.DocumentsResponse
import com.digital.sofia.domain.models.documents.DocumentsWithPaginationModel

class DocumentsResponseMapper(
    private val documentResponseMapper: DocumentResponseMapper
) :
    BaseMapper<DocumentsResponse, DocumentsWithPaginationModel>() {

    override fun map(from: DocumentsResponse): DocumentsWithPaginationModel {
        return DocumentsWithPaginationModel(
            documents = buildList {
                from.documents?.forEach {
                    val document = documentResponseMapper.map(it)
                    if (document.evrotrustTransactionId.isNotEmpty()) {
                        add(document)
                    }
                }
            },
            cursor = from.pagination?.cursor,
        )
    }
}