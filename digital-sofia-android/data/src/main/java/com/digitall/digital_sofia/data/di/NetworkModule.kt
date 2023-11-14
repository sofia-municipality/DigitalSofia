package com.digitall.digital_sofia.data.di

import com.digitall.digital_sofia.data.DEBUG_MOCK_INTERCEPTOR_ENABLED
import com.digitall.digital_sofia.data.URL_AUTH
import com.digitall.digital_sofia.data.URL_BASE
import com.digitall.digital_sofia.data.network.utils.HeaderInterceptor
import com.digitall.digital_sofia.data.network.utils.MockInterceptor
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logNetwork
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * single<Class>(named("name")){Class()} -> for creating a specific instance in module
 * single<Class1>{Class1(get<Class2>(named("name")))} -> for creating a specific instance in module
 * val nameOfVariable: Class by inject(named("name")) -> for creating a specific instance in class
 * get<Class>{parametersOf("param")} -> parameter passing in module
 * single<Class>{(param: String)->Class(param)} -> parameter passing in module
 * val name: Class by inject(parameters={parametersOf("param")}) -> parameter passing in class
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

private const val TAG = "NetworkModuleTag"

const val RETROFIT_BASE = "RETROFIT_BASE"
const val RETROFIT_AUTH = "RETROFIT_AUTH"

const val LOGGING_INTERCEPTOR = "LOGGING_INTERCEPTOR"
const val LOG_TO_FILE_INTERCEPTOR = "LOG_TO_FILE_INTERCEPTOR"

const val TIMEOUT = 60L

val networkModule = module {
    single<Converter.Factory> {
        GsonConverterFactory.create() as Converter.Factory
    }
    single<HttpLoggingInterceptor>(named(LOGGING_INTERCEPTOR)) {
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    single<HttpLoggingInterceptor>(named(LOG_TO_FILE_INTERCEPTOR)) {
        val logging = HttpLoggingInterceptor {
            logNetwork(it)
        }
        logging.level = HttpLoggingInterceptor.Level.BODY
        logging
    }
    single<HeaderInterceptor> {
        HeaderInterceptor(
            preferences = get<PreferencesRepository>(),
        )
    }
    single<MockInterceptor> {
        MockInterceptor()
    }
    single<OkHttpClient> {
        logDebug("create OkHttpClient", TAG)
        OkHttpClient.Builder().apply {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                    // NO IMPLEMENTATION
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                    // NO IMPLEMENTATION
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            )
            val protocolSSL = "SSL"
            val sslContext = SSLContext.getInstance(protocolSSL).apply {
                init(null, trustAllCerts, SecureRandom())
            }
            sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            hostnameVerifier(HostnameVerifier { _, _ -> true })
            addInterceptor(get<HeaderInterceptor>())
            addInterceptor(get<HttpLoggingInterceptor>(named(LOGGING_INTERCEPTOR)))
            addInterceptor(get<HttpLoggingInterceptor>(named(LOG_TO_FILE_INTERCEPTOR)))
            if (DEBUG_MOCK_INTERCEPTOR_ENABLED) {
                logDebug("add MockInterceptor", TAG)
                addInterceptor(get<MockInterceptor>())
            }
            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            readTimeout(TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }
    single<Retrofit>(named(RETROFIT_BASE)) {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(get<OkHttpClient>())
            .addConverterFactory(get())
            .build()
    }
    single<Retrofit>(named(RETROFIT_AUTH)) {
        Retrofit.Builder()
            .baseUrl(URL_AUTH)
            .client(get<OkHttpClient>())
            .addConverterFactory(get())
            .build()
    }
}