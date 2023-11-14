package com.digitall.digital_sofia.data.mappers.network.registration

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.data.models.network.registration.CheckPinResponse
import com.digitall.digital_sofia.domain.models.registration.CheckPinModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

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