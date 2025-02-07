package com.digital.sofia.data.mappers.network.user.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.user.UserProfileStatusChangesRequestBody
import com.digital.sofia.domain.models.user.UserProfileStatusChangesRequestModel

class UserProfileStatusChangesRequestBodyMapper :
    BaseMapper<UserProfileStatusChangesRequestModel, UserProfileStatusChangesRequestBody>() {

    override fun map(from: UserProfileStatusChangesRequestModel): UserProfileStatusChangesRequestBody {
        return with(from) {
            UserProfileStatusChangesRequestBody(identificationNumber = identificationNumber)
        }
    }

}