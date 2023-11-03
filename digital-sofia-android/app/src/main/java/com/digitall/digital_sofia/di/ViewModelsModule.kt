package com.digitall.digital_sofia.di

import com.digitall.digital_sofia.data.di.IO_DISPATCHER
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.authorization.AuthorizationUseCase
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.usecase.registration.RegistrationUseCase
import com.digitall.digital_sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digitall.digital_sofia.mappers.common.PermissionNamePmMapper
import com.digitall.digital_sofia.mappers.documents.DocumentsUiMapper
import com.digitall.digital_sofia.mappers.forms.UnsignedDocumentUiMapper
import com.digitall.digital_sofia.ui.activity.main.MainViewModel
import com.digitall.digital_sofia.ui.fragments.auth.AuthEnterCodeFlowViewModel
import com.digitall.digital_sofia.ui.fragments.auth.biometric.AuthEnterBiometricViewModel
import com.digitall.digital_sofia.ui.fragments.auth.forgot.AuthForgotPinViewModel
import com.digitall.digital_sofia.ui.fragments.auth.pin.AuthEnterPinViewModel
import com.digitall.digital_sofia.ui.fragments.conditions.ConditionsFlowViewModel
import com.digitall.digital_sofia.ui.fragments.conditions.conditions.ConditionsViewModel
import com.digitall.digital_sofia.ui.fragments.contacts.ContactsFlowViewModel
import com.digitall.digital_sofia.ui.fragments.contacts.contacts.ContactsViewModel
import com.digitall.digital_sofia.ui.fragments.error.biometric.BiometricErrorBottomSheetViewModel
import com.digitall.digital_sofia.ui.fragments.error.blocked.BlockedFlowViewModel
import com.digitall.digital_sofia.ui.fragments.error.blocked.blocked.BlockedViewModel
import com.digitall.digital_sofia.ui.fragments.faq.FaqFlowViewModel
import com.digitall.digital_sofia.ui.fragments.faq.faq.FaqViewModel
import com.digitall.digital_sofia.ui.fragments.main.MainTabsFlowViewModel
import com.digitall.digital_sofia.ui.fragments.main.documents.DocumentsViewModel
import com.digitall.digital_sofia.ui.fragments.main.documents.preview.DocumentPreviewViewModel
import com.digitall.digital_sofia.ui.fragments.main.request.ServiceRequestViewModel
import com.digitall.digital_sofia.ui.fragments.main.services.MyServicesViewModel
import com.digitall.digital_sofia.ui.fragments.main.signing.SigningViewModel
import com.digitall.digital_sofia.ui.fragments.permissions.PermissionBottomSheetViewModel
import com.digitall.digital_sofia.ui.fragments.registration.RegistrationFlowViewModel
import com.digitall.digital_sofia.ui.fragments.registration.biometric.RegistrationEnableBiometricViewModel
import com.digitall.digital_sofia.ui.fragments.registration.confirm.RegistrationConfirmIdentificationViewModel
import com.digitall.digital_sofia.ui.fragments.registration.disagree.RegistrationDisagreeViewModel
import com.digitall.digital_sofia.ui.fragments.registration.egn.RegistrationEnterEgnViewModel
import com.digitall.digital_sofia.ui.fragments.registration.email.RegistrationEnterEmailViewModel
import com.digitall.digital_sofia.ui.fragments.registration.error.RegistrationErrorViewModel
import com.digitall.digital_sofia.ui.fragments.registration.pin.create.RegistrationCreatePinViewModel
import com.digitall.digital_sofia.ui.fragments.registration.pin.enter.RegistrationEnterPinViewModel
import com.digitall.digital_sofia.ui.fragments.registration.ready.RegistrationReadyViewModel
import com.digitall.digital_sofia.ui.fragments.registration.register.RegistrationRegisterNewUserViewModel
import com.digitall.digital_sofia.ui.fragments.registration.share.RegistrationShareYourDataViewModel
import com.digitall.digital_sofia.ui.fragments.registration.start.RegistrationStartViewModel
import com.digitall.digital_sofia.ui.fragments.settings.SettingsFlowViewModel
import com.digitall.digital_sofia.ui.fragments.settings.auth.SettingsAuthMethodViewModel
import com.digitall.digital_sofia.ui.fragments.settings.language.SettingsLanguageViewModel
import com.digitall.digital_sofia.ui.fragments.settings.pin.ChangePinFlowViewModel
import com.digitall.digital_sofia.ui.fragments.settings.pin.biometric.ChangePinEnableBiometricViewModel
import com.digitall.digital_sofia.ui.fragments.settings.pin.create.ChangePinCreateViewModel
import com.digitall.digital_sofia.ui.fragments.settings.pin.enter.ChangePinEnterViewModel
import com.digitall.digital_sofia.ui.fragments.settings.profile.ProfileViewModel
import com.digitall.digital_sofia.ui.fragments.settings.settings.SettingsViewModel
import com.digitall.digital_sofia.ui.fragments.start.StartFlowViewModel
import com.digitall.digital_sofia.ui.fragments.start.splash.SplashViewModel
import com.digitall.digital_sofia.utils.ActivitiesCommonHelper
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.DownloadHelper
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.ScreenshotsDetector
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
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

val viewModelsModule = module {
    viewModel {
        MainViewModel(
            rootBeer = get<RootBeer>(),
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            screenshotsDetector = get<ScreenshotsDetector>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            activitiesCommonHelper = get<ActivitiesCommonHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            dispatcherIO = get<CoroutineDispatcher>(named(IO_DISPATCHER)),
        )
    }
    viewModel {
        MainTabsFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            documentsUseCase = get<DocumentsUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationErrorViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        DocumentsViewModel(
            mapper = get<DocumentsUiMapper>(),
            logoutUseCase = get<LogoutUseCase>(),
            downloadHelper = get<DownloadHelper>(),
            preferences = get<PreferencesRepository>(),
            documentsUseCase = get<DocumentsUseCase>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SigningViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            mapper = get<UnsignedDocumentUiMapper>(),
            documentsUseCase = get<DocumentsUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        MyServicesViewModel(
            preferences = get<PreferencesRepository>(),
            logoutUseCase = get<LogoutUseCase>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ServiceRequestViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel{
        DocumentPreviewViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        PermissionBottomSheetViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            permissionNamePmMapper = get<PermissionNamePmMapper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        BiometricErrorBottomSheetViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SettingsFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        FaqFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SettingsViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ContactsViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ContactsFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ConditionsFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        BlockedFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        BlockedViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ConditionsViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        StartFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationStartViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SplashViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationCreatePinViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
        )
    }
    viewModel {
        RegistrationEnterPinViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            registrationUseCase = get<RegistrationUseCase>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationConfirmIdentificationViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationDisagreeViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationEnterEgnViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            registrationUseCase = get<RegistrationUseCase>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationRegisterNewUserViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            registrationUseCase = get<RegistrationUseCase>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationEnterEmailViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        AuthEnterBiometricViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationUseCase = get<AuthorizationUseCase>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationShareYourDataViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            documentsUseCase = get<DocumentsUseCase>(),
            localizationManager = get<LocalizationManager>(),
            registrationUseCase = get<RegistrationUseCase>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        AuthEnterPinViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationUseCase = get<AuthorizationUseCase>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        FaqViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SettingsAuthMethodViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        SettingsLanguageViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ProfileViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationFlowViewModel(
            preferences = get<PreferencesRepository>(),
            logoutUseCase = get<LogoutUseCase>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        AuthForgotPinViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        AuthEnterCodeFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationEnableBiometricViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ChangePinFlowViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        RegistrationReadyViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ChangePinEnterViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
    viewModel {
        ChangePinCreateViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
        )
    }
    viewModel {
        ChangePinEnableBiometricViewModel(
            logoutUseCase = get<LogoutUseCase>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
        )
    }
}