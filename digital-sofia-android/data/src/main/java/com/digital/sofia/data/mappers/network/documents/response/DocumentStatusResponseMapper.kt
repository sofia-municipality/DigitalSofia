package com.digital.sofia.data.mappers.network.documents.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.documents.DocumentStatusResponse
import com.digital.sofia.domain.extensions.getEnumTypeValue
import com.digital.sofia.domain.models.documents.DocumentStatusModel

class DocumentStatusResponseMapper :
    BaseMapper<DocumentStatusResponse, DocumentStatusModel>() {

    override fun map(from: DocumentStatusResponse): DocumentStatusModel {
        return with(from) {
            status?.let { value ->
                getEnumTypeValue<DocumentStatusModel>(value) ?: DocumentStatusModel.UNKNOWN
            } ?: DocumentStatusModel.UNKNOWN
        }
    }
}