/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.network.registration.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.registration.CheckPinResponse
import com.digital.sofia.domain.models.registration.CheckPinModel

class CheckPinResponseMapper :
    BaseMapper<CheckPinResponse, CheckPinModel>() {

    override fun map(from: CheckPinResponse): CheckPinModel {
        return with(from) {
            CheckPinModel(
                matches = matches
            )
        }
    }

}