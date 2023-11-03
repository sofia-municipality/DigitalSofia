package com.digitall.digital_sofia.domain.models.user

import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class UserModel(
    var phone: String?,
    var email: String?,
    var lastName: String?,
    var firstName: String?,
    var isRejected: Boolean?,
    var middleName: String?,
    var countryCode2: String?,
    var countryCode3: String?,
    var isIdentified: Boolean?,
    var lastLatinName: String?,
    var isSupervised: Boolean?,
    var isReadyToSign: Boolean?,
    var firstLatinName: String?,
    var middleLatinName: String?,
    var securityContext: String?,
    var personalIdentificationNumber: String?,
) : Serializable {

    fun validate(): Boolean {
        return isIdentified == true &&
                !personalIdentificationNumber.isNullOrEmpty() &&
                !firstName.isNullOrEmpty() &&
                !lastName.isNullOrEmpty() &&
                !middleName.isNullOrEmpty() &&
                !firstLatinName.isNullOrEmpty() &&
                !lastLatinName.isNullOrEmpty() &&
                !middleLatinName.isNullOrEmpty() &&
                !phone.isNullOrEmpty()
    }

}
