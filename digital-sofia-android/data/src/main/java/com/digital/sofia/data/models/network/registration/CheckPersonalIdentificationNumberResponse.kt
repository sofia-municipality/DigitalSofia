/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.registration

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

data class CheckPersonalIdentificationNumberResponse (
    @SerializedName("userExist") val userExist: Boolean?,
    @SerializedName("hasPin") val hasPin: Boolean?,
    @SerializedName("hasContactInfo") val hasContactInfo: Boolean?,
    @SerializedName("isVerified") val isVerified: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()