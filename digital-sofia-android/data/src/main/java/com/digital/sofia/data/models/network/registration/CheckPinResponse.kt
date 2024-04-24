/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.models.network.registration

import com.digital.sofia.data.models.network.base.BaseResponse
import com.google.gson.annotations.SerializedName

class CheckPinResponse(
    @SerializedName("matches") val matches: Boolean?,
    @SerializedName("type") override val type: String?,
    @SerializedName("message") override val message: String?,
) : BaseResponse()