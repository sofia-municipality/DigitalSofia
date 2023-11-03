package com.digitall.digital_sofia.data.mappers.network.registration

import com.digitall.digital_sofia.data.mappers.base.BaseMapper
import com.digitall.digital_sofia.data.models.network.registration.CheckPersonalIdentificationNumberResponse
import com.digitall.digital_sofia.domain.models.registration.CheckPersonalIdentificationNumberModel

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class CheckPersonalIdentificationNumberResponseMapper :
    BaseMapper<CheckPersonalIdentificationNumberResponse, CheckPersonalIdentificationNumberModel>() {

    override fun map(from: CheckPersonalIdentificationNumberResponse): CheckPersonalIdentificationNumberModel {
        return with(from) {
            CheckPersonalIdentificationNumberModel(
                userExist = userExist,
                hasPin = hasPin,
            )
        }
    }

}