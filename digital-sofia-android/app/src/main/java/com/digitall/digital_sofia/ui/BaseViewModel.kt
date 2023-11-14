package com.digitall.digital_sofia.ui

import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.ErrorState
import com.digitall.digital_sofia.models.common.LoadingState
import com.digitall.digital_sofia.models.common.StringSource
import com.digitall.digital_sofia.models.common.UiState
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.lang.ref.WeakReference
import javax.crypto.Cipher

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

abstract class BaseViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val preferences: PreferencesRepository,
    private val localizationManager: LocalizationManager,
    private val updateDocumentsHelper: UpdateDocumentsHelper,
    private val cryptographyRepository: CryptographyRepository,
) : ViewModel(), KoinComponent {

    companion object {
        private const val TAG = "BaseViewModelTag"
    }

    abstract val needUpdateDocuments: Boolean

    private var flowNavControllerRef: WeakReference<NavController>? = null

    private var activityNavControllerRef: WeakReference<NavController>? = null

    private var isAlreadyInitialized = false

    private val _closeActivityLiveData = SingleLiveEvent<Unit>()
    val closeActivityLiveData = _closeActivityLiveData.readOnly()

    private val _backPressedFailedLiveData = SingleLiveEvent<Unit>()
    val backPressedFailedLiveData = _backPressedFailedLiveData.readOnly()

    private val _showBannerMessageLiveData = SingleLiveEvent<BannerMessage>()
    val showBannerMessageLiveData = _showBannerMessageLiveData.readOnly()

    private val _uiState = MutableLiveData<UiState>()
    val uiState = _uiState.readOnly()

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState = _loadingState.readOnly()

    private val _errorState = MutableLiveData<ErrorState>()
    val errorState = _errorState.readOnly()

    protected fun isMainThread(): Boolean {
        return Thread.currentThread() == Looper.getMainLooper().thread
    }

    fun startUpdateDocumentsIfNeed() {
        if (needUpdateDocuments) {
            logDebug("startUpdateDocuments", TAG)
            updateDocumentsHelper.startUpdateDocuments(viewModelScope)
        }
    }

    fun stopUpdateDocuments() {
        logDebug("stopUpdateDocuments", TAG)
        updateDocumentsHelper.stopUpdateDocuments()
    }

    fun showLoader() {
        if (isMainThread()) {
            _loadingState.value = LoadingState.Loading
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _loadingState.value = LoadingState.Loading
            }
        }
    }

    fun hideLoader() {
        if (isMainThread()) {
            _loadingState.value = LoadingState.Ready
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _loadingState.value = LoadingState.Ready
            }
        }
    }

    fun showEmptyState() {
        logDebug("showEmptyState", TAG)
        if (isMainThread()) {
            _uiState.value = UiState.Empty
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = UiState.Empty
            }
        }
    }

    fun showReadyState() {
        logDebug("showReadyState", TAG)
        if (isMainThread()) {
            _uiState.value = UiState.Ready
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = UiState.Ready
            }
        }
    }

    fun showErrorState(
        iconRes: Int? = null,
        showIcon: Boolean? = null,
        showTitle: Boolean? = null,
        title: StringSource? = null,
        showDescription: Boolean? = null,
        description: StringSource? = null,
        showReloadButton: Boolean? = null,
        reloadButtonText: StringSource? = null,
    ) {
        logDebug("showErrorState", TAG)
        if (isMainThread()) {
            _errorState.value = ErrorState.Error(
                title = title,
                iconRes = iconRes,
                showIcon = showIcon,
                showTitle = showTitle,
                description = description,
                showDescription = showDescription,
                showReloadButton = showReloadButton,
                reloadButtonText = reloadButtonText,

                )
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _errorState.value = ErrorState.Error(
                    title = title,
                    iconRes = iconRes,
                    showIcon = showIcon,
                    showTitle = showTitle,
                    description = description,
                    showDescription = showDescription,
                    showReloadButton = showReloadButton,
                    reloadButtonText = reloadButtonText,
                )
            }
        }
    }

    fun hideErrorState() {
        logDebug("hideErrorState", TAG)
        if (isMainThread()) {
            _errorState.value = ErrorState.Ready
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _errorState.value = ErrorState.Ready
            }
        }
    }

    // Should be called only by Base
    fun attachView() {
        _loadingState.value = LoadingState.Ready
        _errorState.value = ErrorState.Ready
        if (isAlreadyInitialized) return
        isAlreadyInitialized = true
        localizationManager.applyLanguage()
        onFirstAttach()
    }

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

    protected fun showBannerMessage(message: BannerMessage) {
        if (isMainThread()) {
            _showBannerMessageLiveData.value = message
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _showBannerMessageLiveData.value = message
            }
        }
    }

    open fun onBackPressed() {
        if (!findFlowNavController().popBackStack()) {
            _backPressedFailedLiveData.call()
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
        if (isMainThread()) {
            _closeActivityLiveData.call()
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _closeActivityLiveData.call()
            }
        }
    }

    @CallSuper
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        unbindFlowNavController()
        unbindActivityNavController()
//        disposables.clear()
        super.onCleared()
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
        showBannerMessage(BannerMessage.error("User not setup, please go through the registration process"))
        logoutUseCase.logout().onEach { result ->
            result.onLoading {
                logDebug("onLogoutClicked onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("onLogoutClicked onSuccess", TAG)
                hideLoader()
                hideErrorState()
                toRegistrationFragment()
            }.onFailure {
                logError("onLogoutClicked onFailure", TAG)
                hideLoader()
                showErrorState()
            }
        }.launch(viewModelScope)
    }

    fun toRegistrationFragment() {
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toRegistrationFlowFragment(), viewModelScope
        )
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
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
            return null
        }
        if (!pinCode.validateWithEncrypted()) {
            logError("getBiometricCipherForDecryption !pinCode.validateWithEncrypted()", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_pin_code_not_setup))
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