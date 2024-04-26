package com.digital.sofia.data.mappers.network.confirmation.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.confirmation.ConfirmationGenerateCodeResponse
import com.digital.sofia.domain.models.confirmation.ConfirmationGenerateCodeModel

class ConfirmationGenerateCodeResponseMapper :
    BaseMapper<ConfirmationGenerateCodeResponse, ConfirmationGenerateCodeModel>() {

    override fun map(from: ConfirmationGenerateCodeResponse): ConfirmationGenerateCodeModel {
        return with(from) {
            ConfirmationGenerateCodeModel(
                expiresIn = expiresIn,
                codeAlreadySent = codeAlreadySent,
            )
        }
    }

}