package com.digitall.digital_sofia.ui.fragments.auth.pin

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.digitall.digital_sofia.NavActivityDirections
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.domain.extensions.capitalized
import com.digitall.digital_sofia.domain.models.base.onFailure
import com.digitall.digital_sofia.domain.models.base.onLoading
import com.digitall.digital_sofia.domain.models.base.onSuccess
import com.digitall.digital_sofia.domain.models.common.AppLanguage
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.authorization.AuthorizationUseCase
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.launch
import com.digitall.digital_sofia.extensions.navigateInMainThread
import com.digitall.digital_sofia.extensions.navigateNewRootInMainThread
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.ui.fragments.base.pin.enter.BaseEnterCodeViewModel
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class AuthEnterPinViewModel(
    private val preferences: PreferencesRepository,
    private val authorizationUseCase: AuthorizationUseCase,
    logoutUseCase: LogoutUseCase,
    currentContext: CurrentContext,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseEnterCodeViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    currentContext = currentContext,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "EnterPinViewModelTag"
    }

    override val needUpdateDocuments: Boolean = false

    var forceDisableBiometric = false

    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData = _userNameLiveData.readOnly()

    override fun onForgotCodeClicked() {
        logDebug("onForgotCodeClicked", TAG)
        findFlowNavController().navigateInMainThread(
            AuthEnterPinFragmentDirections.toAutoForgotPinFragment(), viewModelScope
        )
    }

    override fun onCodeLocalCheckSuccess(hashedPin: String) {
        logDebug("onCodeLocalCheckSuccess hashedPin: $hashedPin", TAG)
        val user = preferences.readUser()
        if (user == null) {
            logError("onCodeLocalCheckSuccess user == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        if (!user.validate()) {
            logError("onCodeLocalCheckSuccess !user.validate())", TAG)
            showBannerMessage(BannerMessage.error(R.string.error_user_not_setup_correct))
            logout()
            return
        }
        val personalIdentificationNumber = user.personalIdentificationNumber
        if (personalIdentificationNumber.isNullOrEmpty()) {
            logError("onCodeLocalCheckSuccess personalIdentificationNumber.isNullOrEmpty", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            logout()
            return
        }
        authorizationUseCase.enterToAccount(
            hashedPin = hashedPin,
            personalIdentificationNumber = personalIdentificationNumber,
        ).onEach { result ->
            result.onLoading {
                logDebug("enterToAccount onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("enterToAccount onSuccess", TAG)
                navigateNext()
            }.onFailure {
                logError("enterToAccount onFailure", TAG)
                hideLoader()
                resetCodeWhenNeeded()
                showBannerMessage(BannerMessage.error(R.string.auth_enter_pin_error_check_remote))
            }
        }.launch(viewModelScope)
    }

    override fun onFirstAttach() {
        super.onFirstAttach()
        logDebug("onFirstAttach", TAG)
        _userNameLiveData.value = when (preferences.readCurrentLanguage()) {
            AppLanguage.BG -> preferences.readUser()?.firstName?.capitalized() ?: "потребител"
            AppLanguage.EN -> preferences.readUser()?.firstLatinName?.capitalized() ?: "user"
        }
    }

    override fun isBiometricAvailable(context: Context): Boolean {
        return if (forceDisableBiometric) {
            logDebug("isBiometricAvailable false", TAG)
            false
        } else {
            logDebug("isBiometricAvailable checkIsBiometricAvailable", TAG)
            return checkIsBiometricAvailable(context)
        }
    }

    override fun checkCode(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("checkCode decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
        checkCodeLocal(
            hashedPin = hashedPin,
            decryptedPin = decryptedPin,
        )
    }

    override fun navigateNext() {
        logDebug("navigateNext", TAG)
        findActivityNavController().navigateNewRootInMainThread(
            NavActivityDirections.toMainTabsFlowFragment(),
            viewModelScope
        )
        viewModelScope.launch {
            delay(1000)
            hideLoader()
        }
    }

}