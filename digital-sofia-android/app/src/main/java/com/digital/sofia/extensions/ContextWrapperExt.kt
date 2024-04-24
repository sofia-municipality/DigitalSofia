package com.digital.sofia.extensions

import android.content.ContextWrapper
import java.util.Locale

fun ContextWrapper.wrap(language: String): ContextWrapper {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = baseContext.resources.configuration
    config.setLocale(locale)
    val context = baseContext.createConfigurationContext(config)
    return ContextWrapper(context)
}