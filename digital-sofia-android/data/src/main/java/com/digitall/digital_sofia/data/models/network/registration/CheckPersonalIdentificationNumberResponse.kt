package com.digitall.digital_sofia.data.models.network.registration

import com.digitall.digital_sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

data class CheckPersonalIdentificationNumberResponse (
    @SerializedName("userExist") val userExist: Boolean?,
    @SerializedName("hasPin") val hasPin: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()