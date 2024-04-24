package com.digital.sofia.data.mappers.network.documents.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.documents.DocumentAuthenticationRequestBody
import com.digital.sofia.domain.models.documents.DocumentAuthenticationRequestModel

class DocumentAuthenticationRequestBodyMapper :
    BaseMapper<DocumentAuthenticationRequestModel, DocumentAuthenticationRequestBody>() {
    override fun map(from: DocumentAuthenticationRequestModel): DocumentAuthenticationRequestBody {
        return with(from) {
            DocumentAuthenticationRequestBody(
                pin = pin,
                email = email,
                phoneNumber = phoneNumber,
                firebaseToken = firebaseToken
            )
        }
    }
}