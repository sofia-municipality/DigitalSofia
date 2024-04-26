/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.registration.share

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.common.PinCode
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.models.common.ShareDataState
import com.digital.sofia.domain.models.firebase.FirebaseTokenModel
import com.digital.sofia.domain.models.user.UserModel
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.authorization.AuthorizationEnterToAccountUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsAuthenticateDocumentUseCase
import com.digital.sofia.domain.usecase.documents.DocumentsRequestIdentityUseCase
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.registration.RegistrationRegisterNewUserUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import com.digital.sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.flow.onEach

class RegistrationShareYourDataViewModel(
    private val preferences: PreferencesRepository,
    private val authorizationEnterToAccountUseCase: AuthorizationEnterToAccountUseCase,
    private val documentsRequestIdentityUseCase: DocumentsRequestIdentityUseCase,
    private val documentsAuthenticateDocumentUseCase: DocumentsAuthenticateDocumentUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
    updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    getLogLevelUseCase: GetLogLevelUseCase,
    networkConnectionManager: NetworkConnectionManager,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : BaseViewModel(
    loginTimer = loginTimer,
    preferences = preferences,
    appEventsHelper = appEventsHelper,
    authorizationHelper = authorizationHelper,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "RegistrationShareYourDataViewModelTag"
    }

    override val isAuthorizationActive: Boolean = false

    private var evrotrustTransactionId: String? = null

    private var shareDataState = ShareDataState.REQUEST_IDENTITY

    private val _openDocumentViewLiveData = SingleLiveEvent<String>()
    val openDocumentViewLiveData = _openDocumentViewLiveData.readOnly()

    fun proceedNext() {
        logDebug("proceedNext", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("proceedNext pinCode == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        if (!pinCode.validate()) {
            logError("proceedNext !pinCode.validate()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            toRegistrationFragment()
            return
        }
        val user = preferences.readUser()
        if (user == null) {
            logError("proceedNext user == null", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        if (!user.validate()) {
            logError("proceedNext !user.validate())", TAG)
            showMessage(Message.error(R.string.error_user_not_setup_correct))
            toRegistrationFragment()
            return
        }
        val firebaseToken = preferences.readFirebaseToken()
        if (firebaseToken == null || firebaseToken.token.isEmpty()) {
            logError("registerNewUser fcm.isNullOrEmpty", TAG)
        }
        when (shareDataState) {
            ShareDataState.REQUEST_IDENTITY -> {
                logDebug(
                    "proceedNext shareDataState == ShareDataState.REQUEST_IDENTITY",
                    TAG
                )
                // send registration request for new user
                user.personalIdentificationNumber?.let { number ->
                    requestIdentity(
                        personalIdentificationNumber = number
                    )
                }
            }

            ShareDataState.IDENTITY_REQUESTED -> {
                logDebug(
                    "proceedNext shareDataState == AppStatus.IDENTITY_REQUESTED",
                    TAG
                )
                _openDocumentViewLiveData.setValueOnMainThread(evrotrustTransactionId)
            }

            ShareDataState.IDENTITY_SIGNED -> {
                logDebug(
                    "proceedNext shareDataState == AppStatus.IDENTITY_SIGNED",
                    TAG
                )
                evrotrustTransactionId?.let { transactionId ->
                    if (transactionId.isNotEmpty()) {
                        authenticateDocument(
                            pin = pinCode.hashedPin ?: "",
                            email = user.email,
                            phoneNumber = user.phone,
                            firebaseToken = firebaseToken?.token ?: "",
                            evrotrustTransactionId = transactionId
                        )
                    }
                }
            }

            ShareDataState.IDENTITY_AUTHENTICATED -> {
                logDebug(
                    "proceedNext shareDataState == AppStatus.IDENTITY_AUTHENTICATED",
                    TAG
                )

                user.isVerified = true
                preferences.saveUser(user)
                authenticateUser(user = user, pinCode = pinCode)
            }
        }
    }

    private fun requestIdentity(
        personalIdentificationNumber: String
    ) {
        logDebug("requestIdentity personalIdentificationNumber: $personalIdentificationNumber", TAG)
        documentsRequestIdentityUseCase.invoke(
            personalIdentificationNumber = personalIdentificationNumber
        ).onEach { result ->
            result.onLoading {
                logDebug("requestIdentity onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess { document ->
                logDebug("requestIdentity onSuccess", TAG)
                shareDataState = ShareDataState.IDENTITY_REQUESTED
                evrotrustTransactionId = document.evrotrustTransactionId
                proceedNext()
            }.onRetry {
                requestIdentity(personalIdentificationNumber = personalIdentificationNumber)
            }.onFailure {
                logError("requestIdentity onFailure", it, TAG)
                hideLoader()
                showErrorState(
                    iconRes = R.drawable.img_logo_small,
                    description = StringSource.Res(R.string.error_server_error),
                )
            }
        }.launchInScope(viewModelScope)
    }

    private fun authenticateDocument(
        pin: String,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        evrotrustTransactionId: String
    ) {
        documentsAuthenticateDocumentUseCase.invoke(
            pin = pin,
            email = email,
            phoneNumber = phoneNumber,
            firebaseToken = firebaseToken,
            evrotrustTransactionId = evrotrustTransactionId
        ).onEach { result ->
            result.onLoading {
                logDebug("authenticateDocument onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess {
                logDebug("authenticateDocument onSuccess", TAG)
                shareDataState = ShareDataState.IDENTITY_AUTHENTICATED
                proceedNext()
            }.onRetry {
                authenticateDocument(
                    pin = pin,
                    email = email,
                    phoneNumber = phoneNumber,
                    firebaseToken = firebaseToken,
                    evrotrustTransactionId = evrotrustTransactionId
                )
            }.onFailure {
                logError("authenticateDocument onFailure", it, TAG)
                hideLoader()
                showErrorState(
                    iconRes = R.drawable.img_logo_small,
                    description = StringSource.Res(R.string.error_server_error),
                )
            }
        }.launchInScope(viewModelScope)
    }

    private fun authenticateUser(user: UserModel, pinCode: PinCode) {
        logDebug("authenticateUser", TAG)
        val firebaseToken = preferences.readFirebaseToken()
        authorizationEnterToAccountUseCase.invoke(
            hashedPin = pinCode.hashedPin ?: "",
            firebaseToken = firebaseToken?.token ?: "",
            personalIdentificationNumber = user.personalIdentificationNumber ?: ""
        ).onEach { result ->
            result.onLoading {
                logDebug("authenticateUser onLoading", TAG)
                showLoader()
                hideErrorState()
            }.onSuccess {
                logDebug("authenticateUser onSuccess", TAG)
                preferences.saveAppStatus(AppStatus.REGISTERED)
                // check documents to sign
                toRegistrationReadyFragment()
            }.onRetry {
                authenticateUser(user = user, pinCode = pinCode)
            }.onFailure {
                logError("authenticateUser onFailure", it, TAG)
                hideLoader()
                showErrorState(
                    iconRes = R.drawable.img_logo_small,
                    description = StringSource.Res(R.string.error_server_error),
                )
            }
        }.launchInScope(viewModelScope)
    }

    fun toConfirmIdentificationFragment() {
        logDebug("onNoClicked", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationShareYourDataFragmentDirections.toConfirmIdentificationFragment(),
            viewModelScope
        )
    }

    fun onSdkStatusChanged(sdkStatus: SdkStatus) {
        logDebug("onSdkStatusChanged appStatus: ${sdkStatus.name}", TAG)
        when (sdkStatus) {
            SdkStatus.SDK_SETUP_ERROR,
            SdkStatus.USER_SETUP_ERROR,
            SdkStatus.CRITICAL_ERROR -> {
                logout()
            }

            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_CANCELLED,
            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_REJECTED -> {
                preferences.saveAppStatus(AppStatus.NOT_REGISTERED)
                toDisagreeFragment()
            }

            SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY -> {
                if (evrotrustTransactionId.isNullOrEmpty()) {
                    logError(
                        "onSdkStatusChanged evrotrustTransactionId.isNullOrEmpty()",
                        TAG
                    )
                    showMessage(Message.error(R.string.error))
                    toRegistrationFragment()
                    return
                }
                // document signed by sdk but not send to server, send document
                shareDataState = ShareDataState.IDENTITY_SIGNED
                proceedNext()
            }

            else -> {
                // no action
            }
        }
    }

    private fun toDisagreeFragment() {
        logDebug("toDisagreeFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationShareYourDataFragmentDirections.toDisagreeFragment(),
            viewModelScope
        )
    }

    private fun toRegistrationReadyFragment() {
        logDebug("toRegistrationReadyFragment", TAG)
        findFlowNavController().navigateInMainThread(
            RegistrationShareYourDataFragmentDirections.toReadyFragment(),
            viewModelScope
        )
    }
}