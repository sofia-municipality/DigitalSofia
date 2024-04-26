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
package com.digital.sofia.di

import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.authorization.AuthorizationEnterToAccountUseCase
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
import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.mappers.common.PermissionNamePmMapper
import com.digital.sofia.mappers.documents.DocumentsUiMapper
import com.digital.sofia.mappers.forms.UnsignedDocumentUiMapper
import com.digital.sofia.ui.activity.MainViewModel
import com.digital.sofia.ui.fragments.auth.AuthEnterCodeFlowViewModel
import com.digital.sofia.ui.fragments.auth.biometric.AuthEnterBiometricViewModel
import com.digital.sofia.ui.fragments.auth.pin.AuthEnterPinViewModel
import com.digital.sofia.ui.fragments.conditions.ConditionsFlowViewModel
import com.digital.sofia.ui.fragments.conditions.conditions.ConditionsViewModel
import com.digital.sofia.ui.fragments.confirmation.ConfirmationFlowViewModel
import com.digital.sofia.ui.fragments.confirmation.confirmation.ConfirmationViewModel
import com.digital.sofia.ui.fragments.contacts.ContactsFlowViewModel
import com.digital.sofia.ui.fragments.contacts.contacts.ContactsViewModel
import com.digital.sofia.ui.fragments.error.biometric.BiometricErrorBottomSheetViewModel
import com.digital.sofia.ui.fragments.error.blocked.BlockedFlowViewModel
import com.digital.sofia.ui.fragments.error.blocked.blocked.BlockedViewModel
import com.digital.sofia.ui.fragments.faq.FaqFlowViewModel
import com.digital.sofia.ui.fragments.faq.faq.FaqViewModel
import com.digital.sofia.ui.fragments.forgot.ForgotPinRegistrationFlowViewModel
import com.digital.sofia.ui.fragments.forgot.biometric.ForgotPinRegistrationEnableBiometricViewModel
import com.digital.sofia.ui.fragments.forgot.confirm.ForgotPinRegistrationConfirmIdentificationViewModel
import com.digital.sofia.ui.fragments.forgot.create.ForgotPinRegistrationCreatePinViewModel
import com.digital.sofia.ui.fragments.forgot.disagree.ForgotPinDisagreeViewModel
import com.digital.sofia.ui.fragments.forgot.error.ForgotPinRegistrationErrorViewModel
import com.digital.sofia.ui.fragments.main.MainTabsFlowViewModel
import com.digital.sofia.ui.fragments.main.documents.DocumentsViewModel
import com.digital.sofia.ui.fragments.main.documents.preview.DocumentPreviewViewModel
import com.digital.sofia.ui.fragments.main.request.ServiceRequestViewModel
import com.digital.sofia.ui.fragments.main.services.MyServicesViewModel
import com.digital.sofia.ui.fragments.main.signing.SigningViewModel
import com.digital.sofia.ui.fragments.permissions.PermissionBottomSheetViewModel
import com.digital.sofia.ui.fragments.registration.RegistrationFlowViewModel
import com.digital.sofia.ui.fragments.registration.biometric.RegistrationEnableBiometricViewModel
import com.digital.sofia.ui.fragments.registration.confirm.RegistrationConfirmIdentificationViewModel
import com.digital.sofia.ui.fragments.registration.disagree.RegistrationDisagreeViewModel
import com.digital.sofia.ui.fragments.registration.egn.RegistrationEnterEgnViewModel
import com.digital.sofia.ui.fragments.registration.email.RegistrationEnterEmailViewModel
import com.digital.sofia.ui.fragments.registration.error.RegistrationErrorViewModel
import com.digital.sofia.ui.fragments.registration.pin.create.RegistrationCreatePinViewModel
import com.digital.sofia.ui.fragments.registration.pin.enter.RegistrationEnterPinViewModel
import com.digital.sofia.ui.fragments.registration.ready.RegistrationReadyViewModel
import com.digital.sofia.ui.fragments.registration.share.ForgotPinShareYourDataViewModel
import com.digital.sofia.ui.fragments.registration.share.RegistrationShareYourDataViewModel
import com.digital.sofia.ui.fragments.registration.start.RegistrationStartViewModel
import com.digital.sofia.ui.fragments.settings.SettingsFlowViewModel
import com.digital.sofia.ui.fragments.settings.auth.SettingsAuthMethodViewModel
import com.digital.sofia.ui.fragments.settings.delete.profile.confirm.DeleteProfileConfirmViewModel
import com.digital.sofia.ui.fragments.settings.delete.profile.error.DeleteProfileErrorViewModel
import com.digital.sofia.ui.fragments.settings.language.SettingsLanguageViewModel
import com.digital.sofia.ui.fragments.settings.pin.ChangePinFlowViewModel
import com.digital.sofia.ui.fragments.settings.pin.biometric.ChangePinEnableBiometricViewModel
import com.digital.sofia.ui.fragments.settings.pin.create.ChangePinCreateViewModel
import com.digital.sofia.ui.fragments.settings.pin.enter.ChangePinEnterViewModel
import com.digital.sofia.ui.fragments.settings.profile.ProfileViewModel
import com.digital.sofia.ui.fragments.settings.settings.SettingsViewModel
import com.digital.sofia.ui.fragments.start.StartFlowViewModel
import com.digital.sofia.ui.fragments.start.splash.SplashViewModel
import com.digital.sofia.utils.ActivitiesCommonHelper
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.ScreenshotsDetector
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {

    viewModel {
        MainViewModel(
            rootBeer = get<RootBeer>(),
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            screenshotsDetector = get<ScreenshotsDetector>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            activitiesCommonHelper = get<ActivitiesCommonHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            uploadLogsUseCase = get<UploadLogsUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
        )
    }

    viewModel {
        MainTabsFlowViewModel(
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            documentsHaveUnsignedUseCase = get<DocumentsHaveUnsignedUseCase>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            confirmationGetCodeStatusUseCase = get<ConfirmationGetCodeStatusUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
        )
    }

    viewModel {
        RegistrationErrorViewModel(
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        DocumentsViewModel(
            loginTimer = get<LoginTimer>(),
            mapper = get<DocumentsUiMapper>(),
            appEventsHelper = get<AppEventsHelper>(),
            downloadHelper = get<DownloadHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            documentsGetHistoryUseCase = get<DocumentsGetHistoryUseCase>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SigningViewModel(
            loginTimer = get<LoginTimer>(),
            mapper = get<UnsignedDocumentUiMapper>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            documentsSendSignedUseCase = get<DocumentsSendSignedUseCase>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            documentsGetUnsignedUseCase = get<DocumentsGetUnsignedUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        MyServicesViewModel(
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ServiceRequestViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        DocumentPreviewViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            documentsDownloadDocumentUseCase = get<DocumentsDownloadDocumentUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        PermissionBottomSheetViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            permissionNamePmMapper = get<PermissionNamePmMapper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        BiometricErrorBottomSheetViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SettingsFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        FaqFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SettingsViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ContactsViewModel(
            loginTimer = get<LoginTimer>(),

            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ContactsFlowViewModel(
            loginTimer = get<LoginTimer>(),

            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ConditionsFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        BlockedFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        BlockedViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ConditionsViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        StartFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationStartViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SplashViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        RegistrationCreatePinViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        RegistrationEnterPinViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            authorizationEnterToAccountUseCase = get<AuthorizationEnterToAccountUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        RegistrationConfirmIdentificationViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationDisagreeViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationEnterEgnViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            registrationCheckUserUseCase = get<RegistrationCheckUserUseCase>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            registrationRegisterNewUserUseCase = get<RegistrationRegisterNewUserUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        RegistrationEnterEmailViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        AuthEnterBiometricViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        RegistrationShareYourDataViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            documentsRequestIdentityUseCase = get<DocumentsRequestIdentityUseCase>(),
            authorizationEnterToAccountUseCase = get<AuthorizationEnterToAccountUseCase>(),
            documentsAuthenticateDocumentUseCase = get<DocumentsAuthenticateDocumentUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        AuthEnterPinViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        FaqViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SettingsAuthMethodViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        SettingsLanguageViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ProfileViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        DeleteProfileConfirmViewModel(
            loginTimer = get<LoginTimer>(),
            deleteUserUseCase = get<DeleteUserUseCase>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }


    viewModel {
        DeleteProfileErrorViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationFlowViewModel(
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        AuthEnterCodeFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationEnableBiometricViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ChangePinFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        RegistrationReadyViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ChangePinEnterViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ChangePinCreateViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            changePinUseCase = get<ChangePinUseCase>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        ChangePinEnableBiometricViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationEnableBiometricViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationErrorViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            registrationRegisterNewUserUseCase = get<RegistrationRegisterNewUserUseCase>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationCreatePinViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    //

    viewModel {
        ForgotPinRegistrationEnableBiometricViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationConfirmIdentificationViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            evrotrustSDKHelper = get<EvrotrustSDKHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationFlowViewModel(
            loginTimer = get<LoginTimer>(),
            preferences = get<PreferencesRepository>(),
            appEventsHelper = get<AppEventsHelper>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ForgotPinRegistrationCreatePinViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            biometricManager = get<SupportBiometricManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            createCodeResponseErrorToStringMapper = get<CreateCodeResponseErrorToStringMapper>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        ForgotPinShareYourDataViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            authorizationHelper = get<AuthorizationHelper>(),
            localizationManager = get<LocalizationManager>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            documentsRequestIdentityUseCase = get<DocumentsRequestIdentityUseCase>(),
            authorizationEnterToAccountUseCase = get<AuthorizationEnterToAccountUseCase>(),
            documentsAuthenticateDocumentUseCase = get<DocumentsAuthenticateDocumentUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

    viewModel {
        ForgotPinDisagreeViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ConfirmationFlowViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
        )
    }

    viewModel {
        ConfirmationViewModel(
            loginTimer = get<LoginTimer>(),
            appEventsHelper = get<AppEventsHelper>(),
            preferences = get<PreferencesRepository>(),
            localizationManager = get<LocalizationManager>(),
            authorizationHelper = get<AuthorizationHelper>(),
            updateDocumentsHelper = get<UpdateDocumentsHelper>(),
            cryptographyRepository = get<CryptographyRepository>(),
            firebaseMessagingServiceHelper = get<FirebaseMessagingServiceHelper>(),
            confirmationGetCodeStatusUseCase = get<ConfirmationGetCodeStatusUseCase>(),
            confirmationUpdateCodeStatusUseCase = get<ConfirmationUpdateCodeStatusUseCase>(),
            getLogLevelUseCase = get<GetLogLevelUseCase>(),
            networkConnectionManager = get<NetworkConnectionManager>(),
            updateFirebaseTokenUseCase = get<UpdateFirebaseTokenUseCase>(),
        )
    }

}
