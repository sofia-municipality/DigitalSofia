package com.digital.sofia.data.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.digital.sofia.data.extensions.readOnly
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.token.AccessTokenModel
import com.digital.sofia.domain.models.token.RefreshTokenModel
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

class AuthorizationHelperImpl(
    private val preferences: PreferencesRepository,
    private val authorizationNetworkRepository: AuthorizationNetworkRepository,
) : AuthorizationHelper {

    companion object {
        private const val TAG = "AuthorizationHelperTag"
        private const val BUFFER_TIME = 30000L
        private const val INVALID_USER = "User data is incorrect!"
        private const val USER_NOT_EXIST = "User does not exist!"
        private val CRITICAL_SERVER_ERRORS_RANGE = 500..504
    }

    private val _newTokensEventLiveData = SingleLiveEvent<Unit>()
    override val newTokensEventLiveData = _newTokensEventLiveData.readOnly()

    private val _logoutUserEventLiveData = SingleLiveEvent<Unit>()
    override val logoutUserEventLiveData = _logoutUserEventLiveData.readOnly()

    @Volatile
    private var accessTokenJob: Job? = null

    override fun startUpdateAccessTokenTimer(
        accessTokenExpiresIn: Long,
    ) {
        accessTokenJob?.cancel()
        accessTokenJob = CoroutineScope(Dispatchers.IO).launch {
            val timeout = (accessTokenExpiresIn - Date().time) - BUFFER_TIME
            logDebug(
                "startUpdateAccessTokenTimer accessTokenExpiresIn: $accessTokenExpiresIn timeout: $timeout",
                TAG
            )
            runCatchingCancelable {
                delay(timeout)
                startAuthorization()
            }
        }
    }

    override fun stopUpdateTokenTimer() {
        accessTokenJob?.cancel()
    }

    override suspend fun startAuthorization() {
        val user = preferences.readUser()
        if (user == null) {
            logError("enterToAccount user == null", TAG)
            return
        }
        if (!user.validate()) {
            logError("enterToAccount !user.validate())", TAG)
            return
        }
        val personalIdentificationNumber = user.personalIdentificationNumber
        if (personalIdentificationNumber.isNullOrEmpty()) {
            logError("enterToAccount personalIdentificationNumber.isNullOrEmpty", TAG)
            return
        }
        val firebaseToken = preferences.readFirebaseToken()
        if (firebaseToken == null) {
            logError("enterToAccount firebaseToken.isNullOrEmpty", TAG)
            return
        }
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("enterToAccount  pinCode == null", TAG)
            return
        }
        if (!pinCode.validate()) {
            logError("enterToAccount pinCode not valid", TAG)
            return
        }
        authorizationNetworkRepository.enterToAccount(
            hashedPin = pinCode.hashedPin!!,
            firebaseToken = firebaseToken.token,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("enterToAccount onLoading", TAG)
            }.onSuccess {
                val currentTime = Date().time
                if (!it.accessToken.isNullOrEmpty() && it.expiresIn != null && it.expiresIn != 0L) {
                    logDebug(
                        "enterToAccount save accessToken",
                        TAG
                    )
                    val accessTokenModel = AccessTokenModel(
                        token = it.accessToken!!,
                        expirationTime = currentTime + it.expiresIn!! * 1000L,
                    )
                    preferences.saveAccessToken(
                        value = accessTokenModel
                    )
                    logDebug(
                        "enterToAccount startUpdateAccessTokenTimer",
                        TAG
                    )
                    startUpdateAccessTokenTimer(accessTokenModel.expirationTime)
                }
                if (!it.refreshToken.isNullOrEmpty() && it.refreshExpiresIn != null && it.refreshExpiresIn != 0L) {
                    logDebug(
                        "enterToAccount save refreshToken",
                        TAG
                    )
                    val refreshTokenModel = RefreshTokenModel(
                        token = it.refreshToken!!,
                        expirationTime = currentTime + it.refreshExpiresIn!! * 1000L,
                    )
                    preferences.saveRefreshToken(
                        value = refreshTokenModel
                    )
                }
                _newTokensEventLiveData.callOnMainThread()
            }.onFailure {
                logError("enterToAccount onFailure", it, TAG)
                if (it.serverMessage == INVALID_USER ||
                    CRITICAL_SERVER_ERRORS_RANGE.contains(it.responseCode) ||
                    it.serverMessage == USER_NOT_EXIST) {
                    _logoutUserEventLiveData.callOnMainThread()
                    return@onEach
                } else {
                    startAuthorization()
                }
            }
        }.collect()
    }

    private inline fun <R> runCatchingCancelable(block: () -> R): Result<R> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private inner class SingleLiveEvent<T>() : MutableLiveData<T>() {

        private val pending = AtomicBoolean(false)

        constructor(value: T) : this() {
            setValue(value)
        }

        @MainThread
        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            if (hasActiveObservers()) {
                Log.d(
                    "SingleLiveEvent",
                    "Multiple observers registered but only one will be notified of changes."
                )
            }
            super.observe(owner) { value ->
                if (pending.compareAndSet(true, false)) {
                    observer.onChanged(value)
                }
            }
        }

        @MainThread
        override fun setValue(value: T?) {
            pending.set(true)
            super.setValue(value)
        }

        /**
         * Used for cases where T is Void, to make calls cleaner.
         */
        @MainThread
        fun call() {
            value = null
        }

        fun callOnMainThread() {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                value = null
            } else {
                Handler(Looper.getMainLooper()).post {
                    value = null
                }
            }
        }
    }
}