/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.utils

import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import org.koin.core.component.KoinComponent

class HeaderInterceptor(
    private val preferences: PreferencesRepository
) : okhttp3.Interceptor,
    KoinComponent {

    companion object {
        private const val TAG = "HeaderInterceptorTag"
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        logDebug("intercept", TAG)
        val original = chain.request()
        val request = original.newBuilder()
            .method(original.method, original.body)
        val token = preferences.readAccessToken()?.token
        if (!token.isNullOrEmpty()) {
            logDebug("add token: $token", TAG)
            request.header("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}