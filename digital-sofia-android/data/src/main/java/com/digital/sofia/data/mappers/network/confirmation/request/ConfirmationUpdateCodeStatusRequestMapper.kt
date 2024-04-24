package com.digital.sofia.data.mappers.network.confirmation.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.confirmation.ConfirmationUpdateCodeStatusRequestBody
import com.digital.sofia.domain.models.confirmation.ConfirmationUpdateCodeStatusRequestModel

class ConfirmationUpdateCodeStatusRequestMapper: BaseMapper<ConfirmationUpdateCodeStatusRequestModel, ConfirmationUpdateCodeStatusRequestBody>() {
    override fun map(from: ConfirmationUpdateCodeStatusRequestModel): ConfirmationUpdateCodeStatusRequestBody {
        return with(from) {
            ConfirmationUpdateCodeStatusRequestBody(code = code, status = status)
        }
    }
}