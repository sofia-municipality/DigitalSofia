/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.utils

import android.annotation.SuppressLint
import com.digital.sofia.domain.utils.LogUtil.logError
import java.text.SimpleDateFormat

fun isValidOPersonalIdentificationNumber(
    personalIdentificationNumber: String,
): Boolean {
    return personalIdentificationNumber.length == 10
            && checkValidationCode(personalIdentificationNumber)
            && checkValidDate(personalIdentificationNumber)
}

private fun checkValidationCode(personalIdentificationNumber: String): Boolean {
    return try {
        val weight = intArrayOf(2, 4, 8, 5, 10, 9, 7, 3, 6)
        val mySum = weight.indices.sumBy {
            weight[it] * personalIdentificationNumber[it].toString().toInt()
        }
        personalIdentificationNumber.last().toString() == (mySum % 11).toString().last()
            .toString()
    } catch (e: NumberFormatException) {
        logError("checkValidationCode Exception: ${e.message}", e, "checkValidationCode")
        false
    }
}

@SuppressLint("SimpleDateFormat")
private fun checkValidDate(personalIdentificationNumber: String): Boolean {
    try {
        val year = personalIdentificationNumber.substring(0, 2).toInt()
        val month = personalIdentificationNumber.substring(2, 4).toInt()
        val day = personalIdentificationNumber.substring(4, 6).toInt()
        val adjustedYear: Int = when {
            month >= 40 -> year + 2000
            month >= 20 -> year + 1800
            else -> year + 1900
        }
        val dateString = "$adjustedYear-$month-$day"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.isLenient = false
        dateFormat.parse(dateString)
        return true
    } catch (e: Exception) {
        logError("checkValidDate Exception: ${e.message}", e, "checkValidationCode")
        return false
    }
}