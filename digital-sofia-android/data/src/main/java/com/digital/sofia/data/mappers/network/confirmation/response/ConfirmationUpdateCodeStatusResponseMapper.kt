package com.digital.sofia.data.mappers.network.confirmation.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.confirmation.ConfirmationUpdateCodeStatusResponse
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusModel

class ConfirmationUpdateCodeStatusResponseMapper :
    BaseMapper<ConfirmationUpdateCodeStatusResponse, ConfirmationUpdateCodeStatusModel>() {

    override fun map(from: ConfirmationUpdateCodeStatusResponse): ConfirmationUpdateCodeStatusModel {
        return with(from) {
            ConfirmationUpdateCodeStatusModel(
                codeUpdated = codeUpdated,
            )
        }
    }

}