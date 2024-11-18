package com.digital.sofia.extensions

import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

fun ContextWrapper.wrap(language: String): ContextWrapper {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = baseContext.resources.configuration
    config.setLocale(locale)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    val context = baseContext.createConfigurationContext(config)
    return ContextWrapper(context)
}