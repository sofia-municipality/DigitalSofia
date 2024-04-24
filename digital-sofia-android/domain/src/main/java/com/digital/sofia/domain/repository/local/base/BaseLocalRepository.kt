package com.digital.sofia.domain.repository.local.base

import android.os.Bundle
import java.io.Serializable

interface BaseLocalRepository<T> : Serializable {

    fun saveState(bundle: Bundle)

    fun restoreState(bundle: Bundle)

    fun clear()

}