/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.utils

import com.digital.sofia.models.common.AlertDialogResult

interface AlertDialogResultListener {

    fun onAlertDialogResult(result: AlertDialogResult)

}