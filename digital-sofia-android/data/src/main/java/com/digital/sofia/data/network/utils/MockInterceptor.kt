/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.utils

import com.digital.sofia.data.DEBUG_MOCK_INTERCEPTOR_ENABLED
import com.digital.sofia.domain.utils.LogUtil.logDebug
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {

    companion object {
        private const val TAG = "MockInterceptorTag"
    }

    private val mockResponses = mutableMapOf<HttpUrl, MockResponse>().apply {

        "".toHttpUrlOrNull()
            ?.let { url ->
                put(
                    key = url,
                    value = MockResponse(
                        isEnabled = true,
                        body = "",
                        message = "",
                        serverCode = 200,
                    )
                )
            }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val mockResponse = mockResponses[request.url]
        return if (DEBUG_MOCK_INTERCEPTOR_ENABLED &&
            mockResponse != null &&
            mockResponse.isEnabled
        ) {
            logDebug("Intercepted request: ${request.url}", TAG)
            Response.Builder()
                .code(mockResponse.serverCode)
                .message(mockResponse.message)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(
                    mockResponse.body
                        .toByteArray()
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
                .addHeader("Content-type", "application/json")
                .build()
        } else {
            chain.proceed(request)
        }
    }

    private data class MockResponse(
        val isEnabled: Boolean,
        val body: String,
        val message: String,
        val serverCode: Int,
    )

}