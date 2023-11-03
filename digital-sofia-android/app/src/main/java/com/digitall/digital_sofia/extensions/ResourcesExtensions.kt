package com.digitall.digital_sofia.extensions

import android.content.res.Resources
import android.util.DisplayMetrics

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

fun Resources.dpToPx(dp: Float): Float {
    return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Resources.pxToDp(px: Float): Float {
    return px / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}