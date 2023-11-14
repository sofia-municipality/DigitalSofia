package com.digitall.digital_sofia.data.repository.local.base

import android.os.Bundle

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface StateRepository {

    fun saveRepositoryState(bundle: Bundle)

    fun restoreRepositoryState(bundle: Bundle)

}