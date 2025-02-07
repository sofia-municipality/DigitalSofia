/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.ui

import android.content.Intent
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.digital.sofia.NavActivityDirections
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.common.BiometricStatus
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.isFragmentInBackStack
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.navigateInMainThread
import com.digital.sofia.extensions.navigateNewRootInMainThread
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.models.common.ErrorState
import com.digital.sofia.models.common.LoadingState
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.NetworkState
import com.digital.sofia.models.common.NotificationModel
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.models.common.UiState
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.crypto.Cipher

abstract class BaseViewModel(
    private val loginTimer: LoginTimer,
    private val appEventsHelper: AppEventsHelper,
    private val preferences: PreferencesRepository,
    private val localizationManager: LocalizationManager,
    private val cryptographyRepository: CryptographyRepository,
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    private val getLogLevelUseCase: GetLogLevelUseCase,
    private val networkConnectionManager: NetworkConnectionManager,
    authorizationHelper: AuthorizationHelper,
    firebaseMessagingServiceHelper: FirebaseMessagingServiceHelper,
) : ViewModel() {

    companion object {
        private const val TAG = "BaseViewModelTag"
    }

    abstract val isAuthorizationActive: Boolean

    open val isAuthorizationWithAppUpdateEnabled = true

    private var flowNavControllerRef: WeakReference<NavController>? = null

    private var activityNavControllerRef: WeakReference<NavController>? = null

    private var isAlreadyInitialized = false

    private val _closeActivityLiveData = SingleLiveEvent<Unit>()
    val closeActivityLiveData = _closeActivityLiveData.readOnly()

    private val _backPressedFailedLiveData = SingleLiveEvent<Unit>()
    val backPressedFailedLiveData = _backPressedFailedLiveData.readOnly()

    private val _showMessageLiveData = SingleLiveEvent<Message>()
    val showMessageLiveData = _showMessageLiveData.readOnly()

    private val _uiStateLiveData = MutableLiveData<UiState>()
    val uiStateLiveData = _uiStateLiveData.readOnly()

    private val _loadingStateLiveData = MutableLiveData<LoadingState>()
    val loadingStateLiveData = _loadingStateLiveData.readOnly()

    private val _errorStateLiveData = MutableLiveData<ErrorState>()
    val errorStateLiveData = _errorStateLiveData.readOnly()

    private val _networkStateLiveData = MutableLiveData<NetworkState>()
    val networkStateLiveData = _networkStateLiveData.readOnly()

    private val _showBetaStateLiveData = SingleLiveEvent<Unit>()
    val showBetaStateLiveData = _showBetaStateLiveData.readOnly()

    val newAppEventLiveData = appEventsHelper.newAppEventLiveData

    val documentsForSignLiveData = appEventsHelper.documentsForSignLiveData

    val newFirebaseMessageLiveData = firebaseMessagingServiceHelper.newFirebaseMessageLiveData

    val newTokenEventLiveData = firebaseMessagingServiceHelper.newTokenEventLiveData

    val newTokensEventLiveData = authorizationHelper.newTokensEventLiveData

    val logoutUserEventLiveData = authorizationHelper.logoutUserEventLiveData

    val newNetworkConnectionChangeEventLiveData =
        networkConnectionManager.newNetworkConnectionChangeEventLiveData

    fun onNewAuthorizationEvent() {
        logDebug("onNewAuthorizationEvent", TAG)
        if (!isAuthorizationWithAppUpdateEnabled) {
            logError("onNewAuthorizationEvent but !isAuthorizationWithAppUpdateEnabled", TAG)
            return
        }
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toConfirmationFlowFragment(), viewModelScope
        )
    }

    open fun onNewPendingDocumentEvent(isNotificationEvent: Boolean) {
        clearNewPendingDocumentEvent()
    }

    open fun onNewSignedDocumentEvent(isNotificationEvent: Boolean) {
        clearNewSignedDocumentEvent()
    }

    open fun onNewUserProfileStatusChangeEvent(notificationModel: NotificationModel?) {}

    fun stopListenNetworkState() {
        networkConnectionManager.stopListenNetworkState()
    }

    fun clearNewPendingDocumentEvent() {
        appEventsHelper.hasNewPendingDocumentEvent = false
    }

    fun clearNewSignedDocumentEvent() {
        appEventsHelper.hasNewSignedDocumentEvent = false
    }

    fun clearNewUserProfileStatusChangeEvent() {
        appEventsHelper.hasNewUserProfileStatusChangeEvent = false
    }

    fun checkNetworkConnection() {
        hideNoNetworkState()
        networkConnectionManager.checkInternetConnection()
    }

    fun setHasNewUnsignedDocuments(value: Boolean) {
        appEventsHelper.setHasNewUnsignedDocuments(value)
    }

    fun onNewTokenEvent() {
        val firebaseToken = preferences.readFirebaseToken()
        if (firebaseToken != null && !firebaseToken.isSend && isAuthorizationActive) {
            updateFirebaseTokenUseCase.invoke(token = firebaseToken.token).onEach { result ->
                result.onSuccess {
                    preferences.saveFirebaseToken(value = firebaseToken.copy(isSend = true))
                }.onRetry {
                    onNewTokenEvent()
                }.onFailure {
                    logError("updateFirebaseToken onFailure", it, TAG)
                    showMessage(Message.error(R.string.error_server_error))
                }
            }.launchInScope(viewModelScope)
        }
    }

    fun getLogLevel() {
        val user = preferences.readUser()
        if (user != null && !user.personalIdentificationNumber.isNullOrEmpty()) {
            getLogLevelUseCase.invoke(
                personalIdentifier = user.personalIdentificationNumber ?: ""
            ).onEach { result ->
                result.onSuccess { logLevel ->
                    preferences.saveUser(value = user.copy(isDebug = logLevel.level > 0))
                }.onRetry {
                    getLogLevel()
                }.onFailure {
                    logError("updateFirebaseToken onFailure", it, TAG)
                    showMessage(Message.error(R.string.error_server_error))
                }
            }.launchInScope(viewModelScope)
        }
    }

    protected fun isMainThread(): Boolean {
        return Thread.currentThread() == Looper.getMainLooper().thread
    }

    fun onLoginTimerExpired() {
        val appStatus = preferences.readAppStatus()
        val pinCode = preferences.readPinCode()
        val user = preferences.readUser()
        if (appStatus != AppStatus.REGISTERED) {
            logError("onLoginTimerExpired appStatus not ready", TAG)
            toRegistrationFragment()
            return
        }
        if (pinCode == null) {
            logError("onLoginTimerExpired pinCode == null", TAG)
            toRegistrationFragment()
            return
        }
        if (!pinCode.validate()) {
            logError("onLoginTimerExpired !pinCode.validate()", TAG)
            toRegistrationFragment()
            return
        }
        if (user == null) {
            logError("onLoginTimerExpired user == null", TAG)
            toRegistrationFragment()
            return
        }
        if (!user.validate()) {
            logError("onLoginTimerExpired !user.validate()", TAG)
            toRegistrationFragment()
            return
        }
        val activityController = findActivityNavController()
        if (activityController.isFragmentInBackStack(R.id.enterCodeFlowFragment).not()) {
            findActivityNavController().navigateInMainThread(
                NavActivityDirections.toEnterCodeFlowFragment(), viewModelScope
            )
        }
    }

    fun fragmentOnResume() {
        logDebug("fragmentOnResume needUpdateDocuments: $isAuthorizationActive", TAG)
        loginTimer.fragmentOnResume(isAuthorizationActive)
        checkAppEvents()
    }

    fun fragmentOnPause() {
        logDebug("fragmentOnPause", TAG)
    }

    fun showLoader(message: String? = null) {
        _loadingStateLiveData.setValueOnMainThread(LoadingState.Loading(message))
    }

    fun hideLoader() {
        _loadingStateLiveData.setValueOnMainThread(LoadingState.Ready)
    }

    fun showEmptyState() {
        logDebug("showEmptyState", TAG)
        _uiStateLiveData.setValueOnMainThread(UiState.Empty)
    }

    fun showReadyState() {
        logDebug("showReadyState", TAG)
        _uiStateLiveData.setValueOnMainThread(UiState.Ready)
    }

    fun showNoNetworkState() {
        _networkStateLiveData.setValueOnMainThread(NetworkState.Disconnected)
    }

    fun hideNoNetworkState() {
        _networkStateLiveData.setValueOnMainThread(NetworkState.Connected)
    }

    fun showBetaState() {
        logDebug("showBetaState", TAG)
        _showBetaStateLiveData.setValueOnMainThread(null)
    }

    fun showErrorState(
        iconRes: Int? = null,
        showIcon: Boolean? = null,
        showTitle: Boolean? = null,
        title: StringSource? = null,
        showDescription: Boolean? = null,
        description: StringSource? = null,
        showActionOneButton: Boolean? = null,
        showActionTwoButton: Boolean? = null,
        actionOneButtonText: StringSource? = null,
        actionTwoButtonText: StringSource? = null,
    ) {
        logDebug("showErrorState", TAG)
        _errorStateLiveData.setValueOnMainThread(
            ErrorState.Error(
                title = title,
                iconRes = iconRes,
                showIcon = showIcon,
                showTitle = showTitle,
                description = description,
                showDescription = showDescription,
                showActionOneButton = showActionOneButton,
                showActionTwoButton = showActionTwoButton,
                actionOneButtonText = actionOneButtonText,
                actionTwoButtonText = actionTwoButtonText,
            )
        )
    }

    fun hideErrorState() {
        logDebug("hideErrorState", TAG)
        _errorStateLiveData.setValueOnMainThread(ErrorState.Ready)
    }

    // Should be called only by Base
    fun attachView() {
        _loadingStateLiveData.setValueOnMainThread(LoadingState.Ready)
        _errorStateLiveData.setValueOnMainThread(ErrorState.Ready)
        loginTimer.setTimerCoroutineScope(viewModelScope)
        if (isAlreadyInitialized) return
        isAlreadyInitialized = true
        localizationManager.applyLanguage()
        networkConnectionManager.startListenNetworkState()
        onFirstAttach()
    }

    open fun refreshData() {}

    /**
     * You should override this instead of init.
     * This method will be called after [BaseFragment::initViews] and
     * [BaseFlowFragment::onViewCreated] and also after [BaseActivity::onCreate]
     */
    protected open fun onFirstAttach() {}

    /**
     * The flow navigation controller should be set after it is available in the view,
     * so after the view is created.
     */
    fun bindFlowNavController(navController: NavController) {
        flowNavControllerRef = WeakReference(navController)
    }

    /**
     * The flow navigation controller should be removed with the view destruction.
     */
    fun unbindFlowNavController() {
        flowNavControllerRef?.clear()
        flowNavControllerRef = null
    }

    /**
     * The flow navigation controller should be set after it is available in the view,
     * so after the view is created.
     */
    fun bindActivityNavController(navController: NavController) {
        activityNavControllerRef = WeakReference(navController)
    }

    /**
     * The flow navigation controller should be removed with the view destruction.
     */
    fun unbindActivityNavController() {
        activityNavControllerRef?.clear()
        activityNavControllerRef = null
    }

    protected fun findFlowNavController(): NavController {
        return flowNavControllerRef?.get()
            ?: throw IllegalArgumentException("Flow Navigation controller is not set!")
    }

    protected fun findActivityNavController(): NavController {
        return activityNavControllerRef?.get()
            ?: throw IllegalArgumentException("Activity Navigation controller is not set!")
    }

    /**
     * This method takes care of every system error
     * before allowing other parties to react on it.
     *
     * OVERRIDE only if you have to, usually should not be overridden.
     */
    @CallSuper
    open fun consumeException(exception: Throwable, isSilent: Boolean = false) {
        logDebug(exception.toString(), TAG)
    }

    protected fun showMessage(message: Message) {
        _showMessageLiveData.setValueOnMainThread(message)
    }

    open fun onBackPressed() {
        if (!findFlowNavController().popBackStack()) {
            _backPressedFailedLiveData.callOnMainThread()
        }
    }

    open fun finishFlow() {
        if (isMainThread()) {
            if (!findActivityNavController().popBackStack()) {
                closeActivity()
            }
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                if (!findActivityNavController().popBackStack()) {
                    closeActivity()
                }
            }
        }
    }

    protected fun closeActivity() {
        _closeActivityLiveData.callOnMainThread()
    }

    @CallSuper
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        unbindFlowNavController()
        unbindActivityNavController()
//        disposables.clear()
        super.onCleared()
    }

    fun checkAppEvents(notificationModel: NotificationModel? = null) {
        if (isAuthorizationActive) {
            when {
                appEventsHelper.hasNewAuthorizationEvent -> {
                    appEventsHelper.hasNewAuthorizationEvent = false
                    onNewAuthorizationEvent()
                }

                appEventsHelper.hasNewPendingDocumentEvent -> onNewPendingDocumentEvent(
                    isNotificationEvent = appEventsHelper.isNotificationEvent
                )

                appEventsHelper.hasNewSignedDocumentEvent -> onNewSignedDocumentEvent(
                    isNotificationEvent = appEventsHelper.isNotificationEvent
                )
            }
        }

        when {
            appEventsHelper.hasNewUserProfileStatusChangeEvent -> {
                onNewUserProfileStatusChangeEvent(
                    notificationModel = notificationModel
                )
            }
        }
    }

    fun onSettingsClicked() {
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toSettingsFlowFragment(), viewModelScope
        )
    }

    fun onFaqClicked() {
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toFaqFlowFragment(), viewModelScope
        )
    }

    fun onContactsClicked() {
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toContactsFlowFragment(), viewModelScope
        )
    }

    fun onConditionsClicked() {
        findActivityNavController().navigateInMainThread(
            NavActivityDirections.toConditionsFlowFragment(), viewModelScope
        )
    }

    fun logout() {
        logDebug("logout", TAG)
        preferences.logoutFromPreferences()
        toRegistrationFragment()
    }

    fun toRegistrationFragment() {
        preferences.logoutFromPreferences()
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toRegistrationFlowFragment(), viewModelScope
        )
    }

    fun onNewIntent(intent: Intent?) {
        viewModelScope.launch(Dispatchers.IO) {
            appEventsHelper.onNewIntent(intent)
        }
    }

    fun onNewFirebaseMessage(message: RemoteMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            appEventsHelper.onNewFirebaseMessage(message)
        }
    }

    fun enableBiometric(encryptedPin: String) {
        logDebug("setupNow encryptedPin: $encryptedPin", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logout()
            return
        }
        preferences.savePinCode(
            pinCode.copy(
                encryptedPin = encryptedPin,
                biometricStatus = BiometricStatus.BIOMETRIC,
            )
        )
    }

    fun disableBiometric() {
        logDebug("disableBiometric", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logout()
            return
        }
        preferences.savePinCode(
            pinCode.copy(
                encryptedPin = null,
                biometricStatus = BiometricStatus.DENIED,
            )
        )
    }

    protected fun getBiometricCipherForDecryption(): Cipher? {
        logDebug("getBiometricCipherForDecryption", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("getBiometricCipherForDecryption pinCode == null", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            return null
        }
        if (!pinCode.validateWithEncrypted()) {
            logError("getBiometricCipherForDecryption !pinCode.validateWithEncrypted()", TAG)
            showMessage(Message.error(R.string.error_pin_code_not_setup))
            return null
        }
        return try {
            val vector =
                cryptographyRepository.getInitializationVectorFromString(pinCode.encryptedPin!!)
            cryptographyRepository.getBiometricCipherForDecryption(vector)
        } catch (e: Exception) {
            logError("getBiometricCipherForDecryption Exception: ${e.message}", e, TAG)
            null
        }
    }

}