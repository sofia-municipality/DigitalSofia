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

import androidx.security.crypto.EncryptedSharedPreferences
import com.digital.sofia.data.database.dao.documents.DocumentsDao
import com.digital.sofia.data.mappers.database.documents.DocumentsEntityMapper
import com.digital.sofia.data.mappers.network.authorization.AuthorizationResponseMapper
import com.digital.sofia.data.mappers.network.confirmation.request.ConfirmationUpdateCodeStatusRequestMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationGenerateCodeResponseMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationGetCodeStatusResponseMapper
import com.digital.sofia.data.mappers.network.confirmation.response.ConfirmationUpdateCodeStatusResponseMapper
import com.digital.sofia.data.mappers.network.documents.request.DocumentAuthenticationRequestBodyMapper
import com.digital.sofia.data.mappers.network.documents.response.DocumentResponseMapper
import com.digital.sofia.data.mappers.network.documents.response.DocumentsResponseMapper
import com.digital.sofia.data.mappers.network.firebase.request.FirebaseTokenRequestMapper
import com.digital.sofia.data.mappers.network.logs.request.UploadFilesRequestMapper
import com.digital.sofia.data.mappers.network.registration.request.RegisterNewUserRequestMapper
import com.digital.sofia.data.mappers.network.registration.response.CheckPersonalIdentificationNumberResponseMapper
import com.digital.sofia.data.mappers.network.registration.response.CheckPinResponseMapper
import com.digital.sofia.data.mappers.network.settings.request.ChangePinRequestBodyMapper
import com.digital.sofia.data.mappers.network.settings.response.LogLevelResponseMapper
import com.digital.sofia.data.network.authorization.AuthorizationApi
import com.digital.sofia.data.network.common.CommonApi
import com.digital.sofia.data.network.confirmation.ConfirmationApi
import com.digital.sofia.data.network.documents.DocumentsApi
import com.digital.sofia.data.network.logs.LogsApi
import com.digital.sofia.data.network.registration.RegistrationApi
import com.digital.sofia.data.network.settings.SettingsApi
import com.digital.sofia.data.repository.common.CryptographyRepositoryImpl
import com.digital.sofia.data.repository.common.PreferencesRepositoryImpl
import com.digital.sofia.data.repository.database.documents.DocumentsDatabaseRepositoryImpl
import com.digital.sofia.data.repository.local.documents.DocumentsLocalRepositoryImpl
import com.digital.sofia.data.repository.network.authorization.AuthorizationNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.common.CommonNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.confirmation.ConfirmationNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.documents.DocumentsNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.logs.LogsNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.registration.RegistrationNetworkRepositoryImpl
import com.digital.sofia.data.repository.network.settings.SettingsRepositoryImpl
import com.digital.sofia.data.utils.AuthorizationHelperImpl
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.repository.local.documents.DocumentsLocalRepository
import com.digital.sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digital.sofia.domain.repository.network.confirmation.ConfirmationNetworkRepository
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.repository.network.common.CommonNetworkRepository
import com.digital.sofia.domain.repository.network.logs.LogsNetworkRepository
import com.digital.sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.utils.AuthorizationHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repositoryModule = module {

    single<AuthorizationHelper> {
        AuthorizationHelperImpl(
            preferences = get<PreferencesRepository>(),
            authorizationNetworkRepository = get<AuthorizationNetworkRepository>(),
        )
    }

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

    // remote repository

    single<DocumentsNetworkRepository> {
        DocumentsNetworkRepositoryImpl(
            documentsApi = get<DocumentsApi>(),
            documentResponseMapper = get<DocumentResponseMapper>(),
            documentsResponseMapper = get<DocumentsResponseMapper>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            documentAuthenticationRequestBodyMapper = get<DocumentAuthenticationRequestBodyMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }
    single<AuthorizationNetworkRepository> {
        AuthorizationNetworkRepositoryImpl(
            authorizationApi = get<AuthorizationApi>(),
            authorizationResponseMapper = get<AuthorizationResponseMapper>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
        )
    }
    single<RegistrationNetworkRepository> {
        RegistrationNetworkRepositoryImpl(
            registrationApi = get<RegistrationApi>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            checkPersonalIdentificationNumberResponseMapper = get<CheckPersonalIdentificationNumberResponseMapper>(),
            registerNewUserRequestMapper = get<RegisterNewUserRequestMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsApi = get<SettingsApi>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            changePinRequestBodyMapper = get<ChangePinRequestBodyMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }
    single<ConfirmationNetworkRepository> {
        ConfirmationNetworkRepositoryImpl(
            confirmationApi = get<ConfirmationApi>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            confirmationGenerateCodeResponseMapper = get<ConfirmationGenerateCodeResponseMapper>(),
            confirmationGetCodeStatusResponseMapper = get<ConfirmationGetCodeStatusResponseMapper>(),
            confirmationUpdateCodeStatusResponseMapper = get<ConfirmationUpdateCodeStatusResponseMapper>(),
            confirmationUpdateCodeStatusRequestMapper = get<ConfirmationUpdateCodeStatusRequestMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }
    single<CommonNetworkRepository> {
        CommonNetworkRepositoryImpl(
            commonApi = get<CommonApi>(),
            firebaseTokenRequestMapper = get<FirebaseTokenRequestMapper>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            logLevelResponseMapper = get<LogLevelResponseMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }

    single<LogsNetworkRepository> {
        LogsNetworkRepositoryImpl(
            logsApi = get<LogsApi>(),
            coroutineContextProvider = get<CoroutineContextProvider>(),
            uploadFilesRequestMapper = get<UploadFilesRequestMapper>(),
            authorizationHelper = get<AuthorizationHelper>(),
        )
    }

    // remote mappers

    single<DocumentResponseMapper> {
        DocumentResponseMapper()
    }
    single<DocumentsResponseMapper> {
        DocumentsResponseMapper(
            documentResponseMapper = get<DocumentResponseMapper>()
        )
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
    single<DocumentAuthenticationRequestBodyMapper> {
        DocumentAuthenticationRequestBodyMapper()
    }
    single<RegisterNewUserRequestMapper> {
        RegisterNewUserRequestMapper()
    }
    single<ChangePinRequestBodyMapper> {
        ChangePinRequestBodyMapper()
    }

    single<ConfirmationUpdateCodeStatusResponseMapper> {
        ConfirmationUpdateCodeStatusResponseMapper()
    }

    single<ConfirmationGetCodeStatusResponseMapper> {
        ConfirmationGetCodeStatusResponseMapper()
    }

    single<ConfirmationGenerateCodeResponseMapper> {
        ConfirmationGenerateCodeResponseMapper()
    }

    single<ConfirmationUpdateCodeStatusRequestMapper> {
        ConfirmationUpdateCodeStatusRequestMapper()
    }

    single<FirebaseTokenRequestMapper> {
        FirebaseTokenRequestMapper()
    }

    single<UploadFilesRequestMapper> {
        UploadFilesRequestMapper(partName = "files[]")
    }

    single<LogLevelResponseMapper> {
        LogLevelResponseMapper()
    }

}