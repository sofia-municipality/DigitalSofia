package com.digitall.digital_sofia.domain.di

import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digitall.digital_sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digitall.digital_sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digitall.digital_sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digitall.digital_sofia.domain.usecase.authorization.AuthorizationUseCase
import com.digitall.digital_sofia.domain.usecase.authorization.AuthorizationUseCaseImpl
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCaseImpl
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCaseImpl
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCaseImpl
import org.koin.dsl.module

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

val useCaseModule = module {
    factory<DocumentsUseCase> {
        DocumentsUseCaseImpl(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }
    factory<AuthorizationUseCase> {
        AuthorizationUseCaseImpl(
            preferences = get<PreferencesRepository>(),
            authorizationNetworkRepository = get<AuthorizationNetworkRepository>(),
        )
    }
    factory<RegistrationUseCase> {
        RegistrationUseCaseImpl(
            preferences = get<PreferencesRepository>(),
            registrationNetworkRepository = get<RegistrationNetworkRepository>(),
        )
    }
    single<LogoutUseCase> {
        LogoutUseCaseImpl(
            preferences = get<PreferencesRepository>(),
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }
}