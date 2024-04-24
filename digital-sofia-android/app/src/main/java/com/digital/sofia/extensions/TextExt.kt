/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.extensions

import com.digital.sofia.domain.utils.LogUtil.logError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DEFAULT_DATE_INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val DEFAULT_DATE_OUTPUT_FORMAT = "dd.MM.yyyy'г. 'HH:mm Z"

private const val TAG = "TextExtTag"

fun String.convertDate(
    inputFormatString: String = DEFAULT_DATE_INPUT_FORMAT,
    outputFormatString: String = DEFAULT_DATE_OUTPUT_FORMAT,
): String {
    return try {
        val inputFormat = SimpleDateFormat(inputFormatString, Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date? = inputFormat.parse(this)
        val outputFormat = SimpleDateFormat(outputFormatString, Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("GMT+02:00")
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        logError("convertDate Exception: ${e.message}", e, "NavController")
        this
    }
}