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
package com.digital.sofia.domain.di

import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.repository.database.documents.DocumentsDatabaseRepository
import com.digital.sofia.domain.repository.network.authorization.AuthorizationNetworkRepository
import com.digital.sofia.domain.repository.network.confirmation.ConfirmationNetworkRepository
import com.digital.sofia.domain.repository.network.documents.DocumentsNetworkRepository
import com.digital.sofia.domain.repository.network.common.CommonNetworkRepository
import com.digital.sofia.domain.repository.network.logs.LogsNetworkRepository
import com.digital.sofia.domain.repository.network.registration.RegistrationNetworkRepository
import com.digital.sofia.domain.repository.network.settings.SettingsRepository
import com.digital.sofia.domain.usecase.authorization.AuthorizationEnterToAccountUseCase
import com.digital.sofia.domain.usecase.confirmation.ConfirmationGenerateCodeUseCase
import com.digital.sofia.domain.usecase.confirmation.ConfirmationGetCodeStatusUseCase
import com.digital.sofia.domain.usecase.confirmation.ConfirmationUpdateCodeStatusUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsAuthenticateDocumentUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsDownloadDocumentUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsGetHistoryUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsGetUnsignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsHaveUnsignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsRequestIdentityUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsSendSignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsSubscribeToUnsignedUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsSubscribeUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsUpdateUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.logs.UploadLogsUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationCheckUserUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationRegisterNewUserUseCase
import com.digital.sofia.domain.usecase.settings.ChangePinUseCase
import com.digital.sofia.domain.usecase.user.DeleteUserUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import org.koin.dsl.module

val useCaseModule = module {

    // documents

    factory<DocumentsDownloadDocumentUseCase> {
        DocumentsDownloadDocumentUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
        )
    }

    factory<DocumentsSendSignedUseCase> {
        DocumentsSendSignedUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
        )
    }

    factory<DocumentsSubscribeUseCase> {
        DocumentsSubscribeUseCase(
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }

    factory<DocumentsSubscribeToUnsignedUseCase> {
        DocumentsSubscribeToUnsignedUseCase(
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }

    factory<DocumentsHaveUnsignedUseCase> {
        DocumentsHaveUnsignedUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
        )
    }

    factory<DocumentsUpdateUseCase> {
        DocumentsUpdateUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }

    factory<DocumentsDownloadDocumentUseCase> {
        DocumentsDownloadDocumentUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>(),
        )
    }

    factory<ChangePinUseCase> {
        ChangePinUseCase(
            settingsRepository = get<SettingsRepository>(),
        )
    }

    factory<DocumentsRequestIdentityUseCase> {
        DocumentsRequestIdentityUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>()
        )
    }

    factory<DocumentsAuthenticateDocumentUseCase> {
        DocumentsAuthenticateDocumentUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>()
        )
    }

    factory<DocumentsGetUnsignedUseCase> {
        DocumentsGetUnsignedUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>()
        )
    }

    factory<DocumentsGetHistoryUseCase> {
        DocumentsGetHistoryUseCase(
            documentsNetworkRepository = get<DocumentsNetworkRepository>()
        )
    }

    // authorization

    factory<AuthorizationEnterToAccountUseCase> {
        AuthorizationEnterToAccountUseCase(
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            authorizationNetworkRepository = get<AuthorizationNetworkRepository>(),
        )
    }

    // registration

    factory<RegistrationCheckUserUseCase> {
        RegistrationCheckUserUseCase(
            registrationNetworkRepository = get<RegistrationNetworkRepository>(),
        )
    }

    factory<RegistrationRegisterNewUserUseCase> {
        RegistrationRegisterNewUserUseCase(
            registrationNetworkRepository = get<RegistrationNetworkRepository>(),
        )
    }

    // confirmation

    factory<ConfirmationGenerateCodeUseCase> {
        ConfirmationGenerateCodeUseCase(
            confirmationNetworkRepository = get<ConfirmationNetworkRepository>(),
        )
    }

    factory<ConfirmationGetCodeStatusUseCase> {
        ConfirmationGetCodeStatusUseCase(
            confirmationNetworkRepository = get<ConfirmationNetworkRepository>(),
        )
    }

    factory<ConfirmationUpdateCodeStatusUseCase> {
        ConfirmationUpdateCodeStatusUseCase(
            confirmationNetworkRepository = get<ConfirmationNetworkRepository>(),
        )
    }

    // update firebase toke

    factory<UpdateFirebaseTokenUseCase> {
        UpdateFirebaseTokenUseCase(
            commonNetworkRepository = get<CommonNetworkRepository>()
        )
    }

    // logout

    single<LogoutUseCase> {
        LogoutUseCase(
            preferences = get<PreferencesRepository>(),
            settingsRepository = get<SettingsRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }

    // log level

    factory<GetLogLevelUseCase> {
        GetLogLevelUseCase(
            commonNetworkRepository = get<CommonNetworkRepository>()
        )
    }

    // upload logs

    single<UploadLogsUseCase> {
        UploadLogsUseCase(
            logsNetworkRepository = get<LogsNetworkRepository>()
        )
    }

    // delete user

    single<DeleteUserUseCase> {
        DeleteUserUseCase(
            preferences = get<PreferencesRepository>(),
            settingsRepository = get<SettingsRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            documentsDatabaseRepository = get<DocumentsDatabaseRepository>(),
        )
    }

}