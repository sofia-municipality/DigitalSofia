/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.registration

import java.io.Serializable

data class CheckPersonalIdentificationNumberModel(
    val hasPin: Boolean?,
    val userExist: Boolean?,
    val hasContactInfo: Boolean?,
    val isVerified: Boolean?,
) : Serializable