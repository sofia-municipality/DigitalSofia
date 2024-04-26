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
package com.digital.sofia.data.di

import com.digital.sofia.data.BuildConfig.URL_AUTH
import com.digital.sofia.data.BuildConfig.URL_BASE
import com.digital.sofia.data.DEBUG_MOCK_INTERCEPTOR_ENABLED
import com.digital.sofia.data.network.authorization.AuthorizationApi
import com.digital.sofia.data.network.confirmation.ConfirmationApi
import com.digital.sofia.data.network.documents.DocumentsApi
import com.digital.sofia.data.network.common.CommonApi
import com.digital.sofia.data.network.logs.LogsApi
import com.digital.sofia.data.network.registration.RegistrationApi
import com.digital.sofia.data.network.settings.SettingsApi
import com.digital.sofia.data.network.utils.HeaderInterceptor
import com.digital.sofia.data.network.utils.MockInterceptor
import com.digital.sofia.data.network.utils.NullOrEmptyConverterFactory
import com.digital.sofia.data.network.utils.RetryingInterceptor
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logNetwork
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
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "NetworkModuleTag"

const val RETROFIT_BASE = "RETROFIT_BASE"
const val RETROFIT_AUTH = "RETROFIT_AUTH"

const val LOGGING_INTERCEPTOR = "LOGGING_INTERCEPTOR"
const val LOG_TO_FILE_INTERCEPTOR = "LOG_TO_FILE_INTERCEPTOR"

const val TIMEOUT = 60L

val networkModule = module {

    // remote api

    single {
        get<Retrofit>(named(RETROFIT_AUTH)).create(AuthorizationApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_AUTH)).create(RegistrationApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_BASE)).create(DocumentsApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_AUTH)).create(SettingsApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_AUTH)).create(ConfirmationApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_AUTH)).create(CommonApi::class.java)
    }
    single {
        get<Retrofit>(named(RETROFIT_BASE)).create(LogsApi::class.java)
    }

    single<Converter.Factory> {
        GsonConverterFactory.create() as Converter.Factory
    }
    single<NullOrEmptyConverterFactory> {
        NullOrEmptyConverterFactory()
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
    single<RetryingInterceptor> {
        RetryingInterceptor(
            retryCount = 3
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
            followRedirects(true)
            followSslRedirects(true)
            addInterceptor(get<HeaderInterceptor>())
            addInterceptor(get<RetryingInterceptor>())
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
            .addConverterFactory(get<NullOrEmptyConverterFactory>())
            .addConverterFactory(get())
            .build()
    }
    single<Retrofit>(named(RETROFIT_AUTH)) {
        Retrofit.Builder()
            .baseUrl(URL_AUTH)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<NullOrEmptyConverterFactory>())
            .addConverterFactory(get())
            .build()
    }
}