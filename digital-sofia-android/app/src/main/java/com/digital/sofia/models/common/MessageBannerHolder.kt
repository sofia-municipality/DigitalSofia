/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.models.common

import android.view.View

interface MessageBannerHolder {

    fun showMessage(message: Message, anchorView: View? = null)

    fun showBeta()

}