package com.digital.sofia.domain.utils

import androidx.lifecycle.LiveData

interface AuthorizationHelper {

    val newTokensEventLiveData: LiveData<Unit>

    val logoutUserEventLiveData: LiveData<Unit>

    fun startUpdateAccessTokenTimer(
        accessTokenExpiresIn: Long,
    )

    fun stopUpdateTokenTimer()

    suspend fun startAuthorization()

}