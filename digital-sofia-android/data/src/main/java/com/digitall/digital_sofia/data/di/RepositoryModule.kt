package com.digitall.digital_sofia.data.di

import androidx.security.crypto.EncryptedSharedPreferences
import com.digitall.digital_sofia.data.database.dao.documents.DocumentsDao
import com.digitall.digital_sofia.data.mappers.database.documents.DocumentsEntityMapper
import com.digitall.digital_sofia.data.mappers.network.authorization.AuthorizationResponseMapper
import com.digitall.digital_sofia.data.mappers.network.documents.DocumentsResponseMapper
import com.digitall.digital_sofia.data.mappers.network.registration.CheckPersonalIdentificationNumberResponseMapper
import com.digitall.digital_sofia.data.mappers.network.registration.CheckPinResponseMapper
import com.digitall.digital_sofia.data.network.authorization.AuthorizationApi
import com.digitall.digital_sofia.data.network.documents.DocumentsApi
import com.digitall.digital_sofia.data.network.registration.RegistrationApi
import com.digitall.digital_sofia.data.repository.common.CryptographyRepositoryImpl
import com.digitall.digital_sofia.data.repository.common.PreferencesRepositoryImpl
import com.digitall.digital_sofia.data.repository.database.documents.DocumentsDatabaseRepositoryImpl
import com.digitall.digital_sofia.data.repository.local.documents.DocumentsLocalRepositoryImpl
import com.digitall.digital_sofia.data.repository.local.registration.RegistrationLocalRepositoryImpl
import com.digitall.digital_sofia.data.repository.network.authorization.AuthorizationNetworkRepositoryImpl
import com.digitall.digital_sofia.data.repository.network.documents.DocumentsNetworkRepositoryImpl
import com.digitall.digital_sofia.data.repository.network.registration.RegistrationNetworkRepositoryImpl
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digitall.digital_sofia.domain.repository.local.documents.DocumentsLocalRepository
import com.digitall.digital_sofia.domain.repository.local.registration.RegistrationLocalRepository
import com.digitall.digital_sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digitall.digital_sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digitall.digital_sofia.domain.repository.network.registration.RegistrationNetworkRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

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

const val IO_DISPATCHER = "IODispatcher"
const val MAIN_DISPATCHER = "MainDispatcher"

val repositoryModule = module {

    // common repository

    single<CryptographyRepository> {
        CryptographyRepositoryImpl(
            context = androidApplication(),
        )
    }
    single<PreferencesRepository> {
        PreferencesRepositoryImpl(
            preferences = get<EncryptedSharedPreferences>()
        )
    }

    // local repository

    single<DocumentsLocalRepository> {
        DocumentsLocalRepositoryImpl()
    }
    single<RegistrationLocalRepository> {
        RegistrationLocalRepositoryImpl()
    }

    // database repository

    single<DocumentsDatabaseRepository> {
        DocumentsDatabaseRepositoryImpl(
            dao = get<DocumentsDao>(),
            mapper = get<DocumentsEntityMapper>(),
        )
    }

    // database mappers

    single<DocumentsEntityMapper> {
        DocumentsEntityMapper()
    }

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

    // remote repository

    single<DocumentsNetworkRepository> {
        DocumentsNetworkRepositoryImpl(
            documentsApi = get<DocumentsApi>(),
            documentsResponseMapper = get<DocumentsResponseMapper>(),
            dispatcherIO = get<CoroutineDispatcher>(named(IO_DISPATCHER)),
        )
    }
    single<AuthorizationNetworkRepository> {
        AuthorizationNetworkRepositoryImpl(
            api = get<AuthorizationApi>(),
            dispatcherIO = get<CoroutineDispatcher>(named(IO_DISPATCHER)),
            authorizationResponseMapper = get<AuthorizationResponseMapper>(),
        )
    }
    single<RegistrationNetworkRepository> {
        RegistrationNetworkRepositoryImpl(
            documentsApi = get<DocumentsApi>(),
            registrationApi = get<RegistrationApi>(),
            checkPinResponseMapper = get<CheckPinResponseMapper>(),
            dispatcherIO = get<CoroutineDispatcher>(named(IO_DISPATCHER)),
            authorizationResponseMapper = get<AuthorizationResponseMapper>(),
            checkPersonalIdentificationNumberResponseMapper = get<CheckPersonalIdentificationNumberResponseMapper>(),
        )
    }

    // remote mappers

    single<DocumentsResponseMapper> {
        DocumentsResponseMapper()
    }
    single<AuthorizationResponseMapper> {
        AuthorizationResponseMapper()
    }
    single<CheckPersonalIdentificationNumberResponseMapper> {
        CheckPersonalIdentificationNumberResponseMapper()
    }
    single<CheckPinResponseMapper> {
        CheckPinResponseMapper()
    }

}