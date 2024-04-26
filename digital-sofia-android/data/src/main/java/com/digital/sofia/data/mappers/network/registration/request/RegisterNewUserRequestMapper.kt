package com.digital.sofia.data.mappers.network.registration.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.registration.RegisterNewUserRequest
import com.digital.sofia.domain.models.user.RegisterNewUserRequestModel

class RegisterNewUserRequestMapper: BaseMapper<RegisterNewUserRequestModel, RegisterNewUserRequest>() {

    override fun map(from: RegisterNewUserRequestModel): RegisterNewUserRequest {
        return with(from) {
            RegisterNewUserRequest(personalIdentificationNumber = personalIdentificationNumber)
        }
    }
}