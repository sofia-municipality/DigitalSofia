/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.base.pin.create

import androidx.lifecycle.MutableLiveData
import com.digital.sofia.R
import com.digital.sofia.data.models.network.authorization.AuthCreatePassCodeResponseErrorCodes
import com.digital.sofia.domain.extensions.sha256
import com.digital.sofia.domain.models.common.BiometricStatus
import com.digital.sofia.domain.models.common.ErrorStatus
import com.digital.sofia.domain.models.common.PinCode
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.digital.sofia.mappers.common.CreateCodeResponseErrorToStringMapper
import com.digital.sofia.models.common.CreatePinScreenStates
import com.digital.sofia.models.common.InputLockState
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CodeKeyboardHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.SingleLiveEvent
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import java.util.Collections
import kotlin.math.abs

abstract class BaseCreatePinViewModel(
    private val preferences: PreferencesRepository,
    private val biometricManager: SupportBiometricManager,
    private val createCodeResponseErrorToStringMapper: CreateCodeResponseErrorToStringMapper,
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
        private const val TAG = "BaseCreatePinViewModelTag"
        private const val ACCEPTED_CONSECUTIVE_DIGITS_COUNT = 3
    }

    private val codeKeyboardHelper = CodeKeyboardHelper {
        _keyboardLockStateLiveData.setValueOnMainThread(it)
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

    protected abstract val checkPreviousPin: Boolean

    private var firstStepDecryptedPin: String? = null
    var decryptedPin: String? = null
    var hashedPin: String? = null

    protected abstract fun sendNewCodeRemote(
        hashedPin: String,
        decryptedPin: String,
    )

    protected abstract fun navigateNextWithBiometric()

    protected abstract fun navigateNextWithoutBiometric()

    fun validatePin(decryptedPin: String) {
        logDebug("validateCode decryptedPin: $decryptedPin", TAG)
        val screenState = screenStateLiveData.value
        if (screenState == null) {
            logError("validatePin screenState == null", TAG)
            showMessage(Message.error(R.string.error))
            finishFlow()
            return
        }
        when (screenState) {
            CreatePinScreenStates.ENTER -> {
                logDebug("validateCode screenState ENTER", TAG)
                if (!validateNewCode(decryptedPin)) {
                    clearInputOnPinError()
                    return
                }
                firstStepDecryptedPin = decryptedPin
                clearInput()
                setScreenState(CreatePinScreenStates.CONFIRM)
            }

            CreatePinScreenStates.CONFIRM -> {
                logDebug("validateCode screenState CONFIRM", TAG)
                if (firstStepDecryptedPin != decryptedPin) {
                    logError("validateCode firstStepPinEncryptedPin != encryptedPin", TAG)
                    clearInputOnPinError()
                    _passCodesDoNotMatchLiveData.callOnMainThread()
                    return
                }
                val hashedPin = decryptedPin.sha256()
                if (hashedPin.isNullOrEmpty()) {
                    logError("validatePin hashedPin.isNullOrEmpty", TAG)
                    showMessage(Message.error(R.string.error))
                    finishFlow()
                    return
                }
                this.decryptedPin = decryptedPin
                this.hashedPin = hashedPin
                logDebug("validateCode decryptedPin: $decryptedPin\nhashedPin: $hashedPin", TAG)
                clearInput()
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
        if (biometricManager.hasBiometrics()) {
            navigateNextWithBiometric()
        } else {
            navigateNextWithoutBiometric()
        }
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        val screenState = screenStateLiveData.value
        if (screenState == null) {
            logError("onBackPressed screenState == null", TAG)
            showMessage(Message.error(R.string.error))
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
        _errorMessageLiveData.setValueOnMainThread(
            createCodeResponseErrorToStringMapper.map(codeError)
        )
    }

    protected fun setScreenState(screenStates: CreatePinScreenStates) {
        logDebug("setScreenState screenStates: $screenStates", TAG)
        clearInput()
        _screenStateLiveData.setValueOnMainThread(screenStates)
    }

    private fun clearInput() {
        logDebug("clearInput", TAG)
        _clearPassCodeFieldLiveData.callOnMainThread()
        codeKeyboardHelper.lockKeyboardForTimeout(
            durationMilliseconds = 500L,
        )
    }

    private fun clearInputOnPinError() {
        logDebug("clearInputOnPinError", TAG)
        _clearPassCodeFieldLiveData.callOnMainThread()
        _shakePassCodeFieldLiveData.callOnMainThread()
        codeKeyboardHelper.lockKeyboardForTimeout(
            durationMilliseconds = 500L,
        )
    }

    private fun validateNewCode(code: String): Boolean {
        logDebug("validateNewCode code: $code checkPreviousPin: $checkPreviousPin", TAG)
        val oldPin = preferences.readPinCode()?.decryptedPin
        val personalIdentificationNumber = preferences.readUser()?.personalIdentificationNumber
        val (year, month, date) = personalIdentificationNumber?.chunked(2)?.take(3)
            ?: List(3) { "" }
        val lastSix = personalIdentificationNumber?.takeLast(6) ?: ""

        if (checkPreviousPin && code == oldPin) {
            setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_ALREADY_USED)
            return false
        }

        if (code.codePoints().distinct().count().toInt() == 1) {
            setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_REPEATED_DIGITS)
            return false
        }

        val permutationsList = permutations(listOf(date, month, year)).map {
            it.joinToString("")
        }

        if (lastSix == code || permutationsList.contains(code)) {
            setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_HAS_DATE_OF_BIRTH)
            return false
        }

        val isValidRepetition = isValidRepetition(code)
        val hasConsecutiveDigits = hasConsecutiveDigits(code)

        if (isValidRepetition || hasConsecutiveDigits) {
            setCodeResponseError(AuthCreatePassCodeResponseErrorCodes.PASS_CODE_IS_SEQUENT_DIGITS)
            return false
        }

        setScreenState(CreatePinScreenStates.CONFIRM)
        return true
    }

    private fun permutations(input: List<String>): List<List<String>> {
        val solutions = mutableListOf<List<String>>()
        permutationsRecursive(input, 0, solutions)
        return solutions
    }

    private fun permutationsRecursive(
        input: List<String>, index: Int, answers: MutableList<List<String>>
    ) {
        if (index == input.lastIndex) {
            answers.add(input.toList())
        }

        for (idx in index..input.lastIndex) {
            Collections.swap(input, index, idx)
            permutationsRecursive(input, index + 1, answers)
            Collections.swap(input, idx, index)
        }
    }

    private fun isValidRepetition(code: String): Boolean {
        val repetition = findRepetition(code)
        return (repetition.first == 0 || (repetition.first == 1 && repetition.second.length == 1)).not()
    }


    private fun findRepetition(code: String): Pair<Int, String> {
        if (code.isEmpty()) {
            return Pair(0, "")
        }

        val pattern = "([0-9]+)\\1+"
        val regex = Regex(pattern)
        val matcher = regex.findAll(code)
        val matches = mutableListOf<String>()

        matcher.forEach { match ->
            match.groupValues.drop(1).forEach {
                matches.add(it)
            }
        }

        var matchedString = ""
        if (matches.isNotEmpty()) {
            matchedString = matches.first()
        }

        return Pair(matches.size, matchedString)
    }

    private fun hasConsecutiveDigits(code: String): Boolean {
        code.toIntOrNull() ?: return false

        val consecutiveDigitsLists = getConsecutiveDigitsLists(code.map(Char::digitToInt))

        return consecutiveDigitsLists.any { list -> list.size > ACCEPTED_CONSECUTIVE_DIGITS_COUNT }
    }

    /**
     * Collects all consecutive digits in separate lists.
     *
     * @param digits - a list of digits
     * @return a list containing lists of consecutive digits
     */
    private fun getConsecutiveDigitsLists(digits: List<Int>): List<List<Int>> {
        return digits.fold(mutableListOf<MutableList<Int>>()) { accumulator, digit ->

            val isAccumulatorNotInitialized = accumulator.isEmpty()
            // As a consecutive digit is considered such that the absolute difference between the
            // previous and the current is 1. The previous digit is considered to be the last one
            // added to the last list in the initial one
            val isDigitNotConsecutive =
                abs((accumulator.lastOrNull()?.lastOrNull() ?: 0) - digit) != 1

            // Checks if the initial list is initialized and the current digit is consecutive. If
            // the condition is met we add that digit to the last list and if not we initialize a
            // new list and add it to the initial one
            if (isAccumulatorNotInitialized || isDigitNotConsecutive) {
                accumulator.add(mutableListOf(digit))
            } else {
                accumulator.last().add(digit)
            }

            accumulator
        }
    }

}