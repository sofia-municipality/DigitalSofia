/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.network.registration.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.registration.CheckPersonalIdentificationNumberResponse
import com.digital.sofia.domain.models.registration.CheckPersonalIdentificationNumberModel

class CheckPersonalIdentificationNumberResponseMapper :
    BaseMapper<CheckPersonalIdentificationNumberResponse, CheckPersonalIdentificationNumberModel>() {

    override fun map(from: CheckPersonalIdentificationNumberResponse): CheckPersonalIdentificationNumberModel {
        return with(from) {
            CheckPersonalIdentificationNumberModel(
                hasPin = hasPin,
                userExist = userExist,
                hasContactInfo = hasContactInfo,
                isVerified = isVerified,
            )
        }
    }

}