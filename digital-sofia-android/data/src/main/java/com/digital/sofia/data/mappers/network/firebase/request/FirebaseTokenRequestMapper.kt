package com.digital.sofia.data.mappers.network.firebase.request

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.firebase.FirebaseTokenRequestBody
import com.digital.sofia.domain.models.firebase.FirebaseTokenRequestModel

class FirebaseTokenRequestMapper: BaseMapper<FirebaseTokenRequestModel, FirebaseTokenRequestBody>() {
    override fun map(from: FirebaseTokenRequestModel): FirebaseTokenRequestBody {
        return with(from) {
            FirebaseTokenRequestBody(fcm = token)
        }
    }
}