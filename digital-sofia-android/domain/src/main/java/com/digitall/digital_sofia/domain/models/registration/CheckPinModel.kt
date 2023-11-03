package com.digitall.digital_sofia.domain.models.registration

import java.io.Serializable

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class CheckPinModel (
    val matches: Boolean?,
) : Serializable