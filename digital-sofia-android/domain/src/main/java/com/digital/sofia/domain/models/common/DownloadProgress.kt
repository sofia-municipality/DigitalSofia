/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.models.common

import java.io.Serializable

sealed class DownloadProgress : Serializable {

    data class Loading(
        val message: String? = null,
    ) : DownloadProgress()

    data object Ready : DownloadProgress()

}