package com.digital.sofia.data.mappers.network.confirmation.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.confirmation.ConfirmationGetCodeStatusResponse
import com.digital.sofia.domain.models.confirmation.ConfirmationCodeStatusModel

class ConfirmationGetCodeStatusResponseMapper :
    BaseMapper<ConfirmationGetCodeStatusResponse, ConfirmationCodeStatusModel>() {

    override fun map(from: ConfirmationGetCodeStatusResponse): ConfirmationCodeStatusModel {
        return with(from) {
            ConfirmationCodeStatusModel(
                code = code,
                expiresIn = expiresIn,
                codeExists = codeExists,
            )
        }
    }

}