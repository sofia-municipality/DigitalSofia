package com.digitall.digital_sofia.models.common

import android.view.View

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface MessageBannerHolder {

    fun showBannerMessage(message: BannerMessage, anchorView: View? = null)

}