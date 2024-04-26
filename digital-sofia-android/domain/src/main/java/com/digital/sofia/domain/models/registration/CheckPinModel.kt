/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.registration

import java.io.Serializable

data class CheckPinModel (
    val matches: Boolean?,
) : Serializable