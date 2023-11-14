package com.digitall.digital_sofia.ui.fragments.base.pin.create

import androidx.lifecycle.MutableLiveData
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.data.models.network.authorization.AuthCreatePassCodeResponseErrorCodes
import com.digitall.digital_sofia.domain.extensions.sha256
import com.digitall.digital_sofia.domain.models.common.BiometricStatus
import com.digitall.digital_sofia.domain.models.common.ErrorStatus
import com.digitall.digital_sofia.domain.models.common.PinCode
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.logout.LogoutUseCase
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.readOnly
import com.digitall.digital_sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.CreatePinScreenStates
import com.digitall.digital_sofia.models.common.InputLockState
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.CodeKeyboardHelper
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.SingleLiveEvent
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

abstract class BaseCreateCodeViewModel(
    private val preferences: PreferencesRepository,
    private val createCodeResponseErrorToStringMapper: CreateCodeResponseErrorToStringMapper,
    logoutUseCase: LogoutUseCase,
    localizationManager: LocalizationManager,
    updateDocumentsHelper: UpdateDocumentsHelper,
    cryptographyRepository: CryptographyRepository,
) : BaseViewModel(
    preferences = preferences,
    logoutUseCase = logoutUseCase,
    localizationManager = localizationManager,
    updateDocumentsHelper = updateDocumentsHelper,
    cryptographyRepository = cryptographyRepository,
) {

    companion object {
        private const val TAG = "BaseCreateCodeViewModelTag"
    }

    private val codeKeyboardHelper = CodeKeyboardHelper {
        _keyboardLockStateLiveData.value = it
    }

    private val _screenStateLiveData = MutableLiveData(CreatePinScreenStates.ENTER)
    val screenStateLiveData = _screenStateLiveData.readOnly()

    private val _errorMessageLiveData = SingleLiveEvent<String>()
    val errorMessageLiveData = _errorMessageLiveData.readOnly()

    private val _keyboardLockStateLiveData = SingleLiveEvent<InputLockState>()
    val keyboardLockStateLiveData = _keyboardLockStateLiveData.readOnly()

    private val _clearPassCodeFieldLiveData = SingleLiveEvent<Unit>()
    val clearPassCodeFieldLiveData = _clearPassCodeFieldLiveData.readOnly()

    private val _shakePassCodeFieldLiveData = SingleLiveEvent<Unit>()
    val shakePassCodeFieldLiveData = _shakePassCodeFieldLiveData.readOnly()

    private val _passCodesDoNotMatchLiveData = SingleLiveEvent<Unit>()
    val passCodesDoNotMatchLiveData = _passCodesDoNotMatchLiveData.readOnly()

    private var firstStepDecryptedPin: String? = null
    var decryptedPin: String? = null
    var hashedPin: String? = null

    protected abstract fun sendNewCodeRemote(
        hashedPin: String,
        decryptedPin: String,
    )

    protected abstract fun navigateNext()

    fun validatePin(decryptedPin: String) {
        logDebug("validateCode decryptedPin: $decryptedPin", TAG)
        val screenState = screenStateLiveData.value
        if (screenState == null) {
            logError("validatePin screenState == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            finishFlow()
            return
        }
        when (screenState) {
            CreatePinScreenStates.ENTER -> {
                logDebug("validateCode screenState ENTER", TAG)
                firstStepDecryptedPin = decryptedPin
                setScreenState(CreatePinScreenStates.CONFIRM)
                return
            }

            CreatePinScreenStates.CONFIRM -> {
                logDebug("validateCode screenState CONFIRM", TAG)
                if (firstStepDecryptedPin != decryptedPin) {
                    logError("validateCode firstStepPinEncryptedPin != encryptedPin", TAG)
                    clearInputOnPinError()
                    _passCodesDoNotMatchLiveData.call()
                    return
                }
                val hashedPin = decryptedPin.sha256()
                if (hashedPin.isNullOrEmpty()) {
                    logError("validatePin hashedPin.isNullOrEmpty", TAG)
                    showBannerMessage(BannerMessage.error(R.string.error))
                    finishFlow()
                    return
                }
                this.decryptedPin = decryptedPin
                this.hashedPin = hashedPin
                logDebug("validateCode decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
                sendNewCodeRemote(
                    hashedPin = hashedPin,
                    decryptedPin = decryptedPin,
                )
            }
        }
    }

    protected fun onSendNewCodeSuccess(
        hashedPin: String,
        decryptedPin: String,
    ) {
        logDebug("onSendNewCodeSuccess hashedPin: $hashedPin\ndecryptedPin: $decryptedPin", TAG)
        preferences.savePinCode(
            PinCode(
                errorCount = 3,
                encryptedPin = null,
                errorTimeCode = null,
                hashedPin = hashedPin,
                decryptedPin = decryptedPin,
                errorStatus = ErrorStatus.NO_TIMEOUT,
                biometricStatus = BiometricStatus.UNSPECIFIED,
            )
        )
        navigateNext()
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        val screenState = screenStateLiveData.value
        if (screenState == null) {
            logError("onBackPressed screenState == null", TAG)
            showBannerMessage(BannerMessage.error(R.string.error))
            finishFlow()
            return
        }
        when (screenState) {
            CreatePinScreenStates.ENTER -> finishFlow()
            CreatePinScreenStates.CONFIRM -> setScreenState(CreatePinScreenStates.ENTER)
        }
    }

    private fun setCodeResponseError(codeError: AuthCreatePassCodeResponseErrorCodes) {
        logDebug("setCodeResponseError", TAG)
        _errorMessageLiveData.value = createCodeResponseErrorToStringMapper.map(codeError)
    }

    private fun setScreenState(screenStates: CreatePinScreenStates) {
        logDebug("setScreenState screenStates: $screenStates", TAG)
        clearInput()
        _screenStateLiveData.value = screenStates
    }

    private fun clearInput() {
        logDebug("clearInput", TAG)
        _clearPassCodeFieldLiveData.call()
        codeKeyboardHelper.lockKeyboardForTimeout(
            durationMilliseconds = 500L,
        )
    }

    private fun clearInputOnPinError() {
        logDebug("clearInputOnPinError", TAG)
        _clearPassCodeFieldLiveData.call()
        _shakePassCodeFieldLiveData.call()
        codeKeyboardHelper.lockKeyboardForTimeout(
            durationMilliseconds = 500L,
        )
    }

    // TODO
    private fun validateNewCode(code: String) {
        logDebug("validateNewCode code: $code", TAG)
        val personalIdentificationNumber = preferences.readUser()?.personalIdentificationNumber
        val firstSix = personalIdentificationNumber?.substring(0, 6) ?: ""
        when (code) {
            "111111",
            "222222",
            "333333",
            "444444",
            "555555",
            "666666",
            "777777",
            "888888",
            "999999",
            "000000" -> {
                setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_REPEATED_DIGITS)
                clearInputOnPinError()
                return
            }

            "123456",
            "654321" -> {
                setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_IS_SEQUENT_DIGITS)
                clearInputOnPinError()
                return
            }

            firstSix -> {
                setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_DATE_OF_BIRTH)
                clearInputOnPinError()
                return
            }

            else -> {
                setScreenState(CreatePinScreenStates.CONFIRM)
            }
        }
    }

}