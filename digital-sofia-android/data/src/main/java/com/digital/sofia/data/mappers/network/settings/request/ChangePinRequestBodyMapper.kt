package com.digital.sofia.data.mappers.network.settings.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.registration.ChangePinRequestBody
import com.digital.sofia.domain.models.settings.ChangePinRequestModel

class ChangePinRequestBodyMapper: BaseMapper<ChangePinRequestModel, ChangePinRequestBody>() {
    override fun map(from: ChangePinRequestModel): ChangePinRequestBody {
        return with(from) {
            ChangePinRequestBody(pin = pin)
        }
    }
}