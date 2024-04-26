package com.digital.sofia.data.network.utils

import okhttp3.Interceptor
import okhttp3.Response

class RetryingInterceptor(
    val retryCount: Int
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return process(chain = chain, attempt = 0)
    }

    private fun process(chain: Interceptor.Chain, attempt: Int): Response {
        var response: Response? = null
        try {
            val request = chain.request()
            response = chain.proceed(request)
            if (attempt < retryCount && !response.isSuccessful) {
                return newAttempt(chain = chain, response = response, attempt = attempt)
            }
            return response
        } catch (exception: Exception) {
            if (attempt < retryCount) {
                return newAttempt(chain = chain, response = response, attempt = attempt)
            }
            throw exception
        }
    }

    private fun newAttempt(chain: Interceptor.Chain, response: Response?, attempt: Int): Response {
        response?.body?.close()
        return process(chain = chain, attempt = attempt + 1)
    }
}