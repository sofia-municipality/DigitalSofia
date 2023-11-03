package com.digitall.digital_sofia.utils

import android.app.Activity
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.data.EVROTRUST_APPLICATION_NUMBER
import com.digitall.digital_sofia.domain.models.common.AppStatus
import com.digitall.digital_sofia.domain.models.common.SdkStatus
import com.digitall.digital_sofia.domain.models.user.UserModel
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.readOnly
import com.evrotrust.lib.EvrotrustSDK
import com.evrotrust.lib.activities.EvrotrustActivity
import com.evrotrust.lib.listeners.EvrotrustChangeSecurityContextListener
import com.evrotrust.lib.listeners.EvrotrustCheckUserStatusListener
import com.evrotrust.lib.listeners.EvrotrustSetupSDKListener
import com.evrotrust.lib.listeners.EvrotrustSubscribeForUserStatusCallbackListener
import com.evrotrust.lib.listeners.EvrotrustUserSetUpListener
import com.evrotrust.lib.listeners.EvrotrustUserSetUpOnlineListener
import com.evrotrust.lib.models.EvrotrustChangeSecurityContextResult
import com.evrotrust.lib.models.EvrotrustCheckUserStatusResult
import com.evrotrust.lib.models.EvrotrustEditPersonalDataResult
import com.evrotrust.lib.models.EvrotrustOpenGroupDocumentsResult
import com.evrotrust.lib.models.EvrotrustOpenSingleDocumentResult
import com.evrotrust.lib.models.EvrotrustSetupProfileResult
import com.evrotrust.lib.models.EvrotrustSetupSDKResult
import com.evrotrust.lib.models.EvrotrustSubscribeForUserStatusCallbackResult
import com.evrotrust.lib.models.EvrotrustUserSetUpOnlineResult
import com.evrotrust.lib.models.EvrotrustUserSetUpResult
import com.evrotrust.lib.services.EvrotrustCustomization
import com.evrotrust.lib.utils.EvrotrustConstants
import com.evrotrust.lib.utils.EvrotrustConstants.UserDecision

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface EvrotrustSDKHelper {

    val sdkStatusLiveData: LiveData<SdkStatus>

    val errorMessageResLiveData: LiveData<Int>

    var errorMessageRes: Int?

    // actions

    fun setupSdk()

    fun isSDKSetUp(): Boolean

    fun startSetupUserActivity(
        activity: Activity,
    )

    fun setupDeviceWithEvrotrustProfile(
        activity: Activity,
    )

    fun checkUserStatus()

    fun isUserSetUp()

    fun isUserSetUpOnline()

    fun openDocument(
        activity: Activity,
        evrotrustTransactionId: String,
    )

    fun openGroupOfDocuments(
        activity: Activity,
        evrotrustTransactionId: String,
    )

    fun changeSecurityContext(
        newSecurityContext: String,
    )

    fun openSettingsScreens(
        activity: Activity,
    )

    fun openEditProfile(
        activity: Activity,
    )

    fun subscribeForUserStatusCallback()

    // activity result

    fun onActivityResult(
        requestCode: Int,
        data: Intent?,
    )

}

class EvrotrustSDKHelperImpl(
    private val currentContext: CurrentContext,
    private val preferences: PreferencesRepository,
) : EvrotrustSDKHelper,
    EvrotrustSetupSDKListener,
    EvrotrustCheckUserStatusListener,
    EvrotrustChangeSecurityContextListener,
    EvrotrustSubscribeForUserStatusCallbackListener,
    EvrotrustUserSetUpListener,
    EvrotrustUserSetUpOnlineListener {

    companion object {
        private const val TAG = "EvrotrustSDKHelperTag"
    }

    override var errorMessageRes: Int? = null

    private val _sdkStatusLiveData = SingleLiveEvent<SdkStatus>()
    override val sdkStatusLiveData = _sdkStatusLiveData.readOnly()

    private val _errorMessageResLiveData = SingleLiveEvent<Int>()
    override val errorMessageResLiveData = _errorMessageResLiveData.readOnly()

    override fun setupSdk() {
        logDebug("setupSdk", TAG)
        EvrotrustSDK.getInstance(currentContext.get())
            .setupSDK(
                EVROTRUST_APPLICATION_NUMBER,
                currentContext.get(),
                EvrotrustConstants.ENVIRONMENT_TEST,
                this // EvrotrustSetupSDKResult
            )
        val customization = EvrotrustCustomization().apply {
            imageCustomization.questionsTitleImage = R.drawable.img_logo_big
            imageCustomization.contactsTitleImage = R.drawable.img_logo_big
            imageCustomization.documentsTitleImage = R.drawable.img_logo_big
            imageCustomization.scanInstructionsImage = R.drawable.img_logo_big
            imageCustomization.isWhiteEvrotrustLogo = true
            nightMode = EvrotrustConstants.NightMode.MODE_NIGHT_NO
//            fontName = "sofia_sans_regular.ttf"
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .setCustomization(customization)
    }

    override fun evrotrustSetupSDKResult(
        result: EvrotrustSetupSDKResult,
    ) {
        logDebug("evrotrustSetupSDKResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("evrotrustSetupSDKResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("evrotrustSetupSDKResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError("evrotrustSetupSDKResult status ERROR_INPUT", TAG)
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError("evrotrustSetupSDKResult status USER_CANCELED", TAG)
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    val isSetUp = result.isSetUp
                    if (!isSetUp) {
                        logError("setupSdkResult status OK but !isSetUp", TAG)
                        setupErrorMessage(R.string.sdk_error_unknown)
                        SdkStatus.USER_SETUP_ERROR
                    } else {
                        logDebug("setupSdkResult status OK", TAG)
                        SdkStatus.SDK_SETUP_READY
                    }
                }

                else -> {
                    logError("evrotrustSetupSDKResult status else", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }

    override fun startSetupUserActivity(
        activity: Activity,
    ) {
        logDebug("startSetupUserActivity", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("startSetupUserActivity pinCode == nul", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!pinCode.validate()) {
            logError("startSetupUserActivity !pinCode.validate()", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        val user = preferences.readUser()
        if (user == null) {
            logError("startSetupUserActivity user == null", TAG)
            setupErrorMessage(R.string.sdk_error_critical)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (user.personalIdentificationNumber.isNullOrEmpty()) {
            logError(
                "startSetupUserActivity user.personalIdentificationNumber.isNullOrEmpty()",
                TAG
            )
            setupErrorMessage(R.string.sdk_error_critical)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("startSetupUserActivity not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.SETUP_PROFILE
            )
            putExtra(
                EvrotrustConstants.EXTRA_IS_ACTING_AS_REGISTRATION_AUTHORITY,
                false
            ) // always has to be false
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                pinCode.hashedPin
            ) // optional parameter
            putExtra(
                EvrotrustConstants.EXTRA_SHOULD_SKIP_CONTACT_INFORMATION,
                false
            )
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_TYPE,
                EvrotrustConstants.USER_TYPE_IDENTIFICATION_NUMBER
            )
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_VALUE,
                user.personalIdentificationNumber
            )
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_COUNTRY_CODE3,
                "BGR"
            )
        }
        activity.startActivityForResult(
            intent,
            EvrotrustConstants.REQUEST_CODE_SETUP_PROFILE,
        )
    }

    override fun setupDeviceWithEvrotrustProfile(
        activity: Activity,
    ) {
        logDebug("setupDeviceWithEvrotrustProfile", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("setupDeviceWithEvrotrustProfile pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!pinCode.validate()) {
            logError("setupDeviceWithEvrotrustProfile !pinCode.validate()", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        val user = preferences.readUser()
        if (user == null) {
            logError("setupDeviceWithEvrotrustProfile user == null", TAG)
            setupErrorMessage(R.string.sdk_error_critical)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!user.validate()) {
            logError("setupDeviceWithEvrotrustProfile !user.validate()", TAG)
            setupErrorMessage(R.string.sdk_error_critical)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("setupDeviceWithEvrotrustProfile not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.SETUP_PROFILE
            )
            putExtra(
                EvrotrustConstants.EXTRA_IS_ACTING_AS_REGISTRATION_AUTHORITY,
                false
            ) // always has to be false
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                pinCode.hashedPin
            ) // optional parameter
            putExtra(
                EvrotrustConstants.EXTRA_SHOULD_SKIP_CONTACT_INFORMATION,
                false
            ) // optional parameter
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_TYPE,
                EvrotrustConstants.USER_TYPE_IDENTIFICATION_NUMBER
            )
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_VALUE,
                user.personalIdentificationNumber
            )
            putExtra(
                EvrotrustConstants.EXTRA_PERSONAL_IDENTIFICATION_NUMBER,
                user.personalIdentificationNumber
            )
            putExtra(
                EvrotrustConstants.EXTRA_PHONE_NUMBER,
                user.phone
            )
            putExtra(
                EvrotrustConstants.EXTRA_EMAIL_ADDRESS,
                user.email
            )
            putExtra(
                EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_COUNTRY_CODE3,
                "BGR"
            )
        }
        activity.startActivityForResult(intent, EvrotrustConstants.REQUEST_CODE_SETUP_PROFILE)
    }

    override fun checkUserStatus() {
        logDebug("checkUserStatus", TAG)
        if (!isSDKSetUp()) {
            logError("checkUserStatus not isSDKSetUp", TAG)
            setupSdk()
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .checkUserStatus(
                currentContext.get(),
                this // EvrotrustCheckUserStatusResult
            )
    }

    override fun isUserSetUp() {
        logDebug("isUserSetUp", TAG)
        if (!isSDKSetUp()) {
            logError("isUserSetUp not isSDKSetUp", TAG)
            setupSdk()
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .isUserSetUp(
                currentContext.get(),
                this // EvrotrustUserSetUpResult
            )
    }

    override fun isUserSetUpOnline() {
        logDebug("isUserSetUpOnline", TAG)
        if (!isSDKSetUp()) {
            logError("isUserSetUpOnline not isSDKSetUp", TAG)
            setupSdk()
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .isUserSetUpOnline(
                currentContext.get(),
                this // EvrotrustUserSetUpOnlineResult
            )
    }

    override fun еvrotrustCheckUserStatusResult(
        result: EvrotrustCheckUserStatusResult
    ) {
        logDebug("еvrotrustCheckUserStatusResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("еvrotrustCheckUserStatusResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("еvrotrustCheckUserStatusResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError("еvrotrustCheckUserStatusResult status ERROR_INPUT", TAG)
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError("еvrotrustCheckUserStatusResult status USER_CANCELED", TAG)
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    if (!result.isSuccessfulCheck) {
                        logError(
                            "еvrotrustCheckUserStatusResult status OK but not successfulCheck",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_unknown)
                        SdkStatus.USER_SETUP_ERROR
                    } else {
                        logDebug("checkUserStatusResult status OK successfulCheck", TAG)
                        val isIdentified = result.isIdentified
                        val isSupervised = result.isSupervised
                        val isReadyToSign = result.isReadyToSign
                        val isRejected = result.isRejected
                        val hasConfirmedPhone = result.hasConfirmedPhone()
                        val hasConfirmedEmail = result.hasConfirmedEmail()
                        SdkStatus.USER_STATUS_READY
                    }
                }

                else -> {
                    logError("еvrotrustCheckUserStatusResult status null", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }

    override fun subscribeForUserStatusCallback() {
        logDebug("subscribeForUserStatusCallback", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("subscribeForUserStatusCallback pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("subscribeForUserStatusCallback not isSDKSetUp", TAG)
            setupSdk()
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .subscribeForUserStatusCallback(
                currentContext.get(),
                "https://example.com",
                pinCode.hashedPin,
                this // EvrotrustSubscribeForUserStatusCallbackResult
            )
    }

    override fun еvrotrustSubscribeForUserStatusCallbackResult(
        result: EvrotrustSubscribeForUserStatusCallbackResult,
    ) {
        logDebug("subscribeForUserStatusCallbackResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError(
                        "еvrotrustSubscribeForUserStatusCallbackResult status USER_NOT_SET_UP",
                        TAG
                    )
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError(
                        "еvrotrustSubscribeForUserStatusCallbackResult status SDK_NOT_SET_UP",
                        TAG
                    )
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError(
                        "еvrotrustSubscribeForUserStatusCallbackResult status ERROR_INPUT",
                        TAG
                    )
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError(
                        "еvrotrustSubscribeForUserStatusCallbackResult status USER_CANCELED",
                        TAG
                    )
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    if (!result.isSuccessful) {
                        logError(
                            "еvrotrustSubscribeForUserStatusCallbackResult status OK but not successfulCheck",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_unknown)
                        SdkStatus.USER_SETUP_ERROR
                    } else {
                        val isSubscribed = result.isSubscribed
                        val isIdentified = result.isIdentified
                        val isSupervised = result.isSupervised
                        val isReadyToSign = result.isReadyToSign
                        val isRejected = result.isRejected
                        logDebug(
                            "subscribeForUserStatusCallbackResult status OK successfulCheck\n" +
                                    "isSubscribed: $isSubscribed\n" +
                                    "isIdentified: $isIdentified\n" +
                                    "isSupervised: $isSupervised\n" +
                                    "isReadyToSign: $isReadyToSign\n" +
                                    "isRejected: $isRejected\n",
                            TAG
                        )
                        SdkStatus.USER_STATUS_CALLBACK_READY
                    }
                }

                else -> {
                    logError("еvrotrustSubscribeForUserStatusCallbackResult status null", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }

    override fun еvrotrustUserSetUpResult(
        result: EvrotrustUserSetUpResult,
    ) {
        logDebug("еvrotrustUserSetUpResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError("еvrotrustUserSetUpResult status ERROR_INPUT", TAG)
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError("еvrotrustUserSetUpResult status USER_CANCELED", TAG)
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    if (!result.isUserSetUp) {
                        logError(
                            "еvrotrustUserSetUpResult status OK but not isUserSetUp",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_unknown)
                        SdkStatus.USER_SETUP_ERROR
                    } else {
                        logDebug("еvrotrustUserSetUpResult status OK", TAG)
                        SdkStatus.USER_SETUP_READY
                    }
                }

                else -> {
                    logError("еvrotrustUserSetUpResult status null", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }

    override fun еvrotrustUserSetUpOnlineResult(
        result: EvrotrustUserSetUpOnlineResult,
    ) {
        logDebug("еvrotrustUserSetUpOnlineResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpOnlineResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpOnlineResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError("еvrotrustUserSetUpOnlineResult status ERROR_INPUT", TAG)
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError("еvrotrustUserSetUpOnlineResult status USER_CANCELED", TAG)
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    when {
                        !result.isSuccessfulCheck -> {
                            logError(
                                "еvrotrustUserSetUpOnlineResult !result.isSuccessfulCheck",
                                TAG
                            )
                            setupErrorMessage(R.string.sdk_error_unknown)
                            SdkStatus.USER_SETUP_ERROR
                        }

                        !result.isUserSetUp -> {
                            logError(
                                "еvrotrustUserSetUpOnlineResult !result.isUserSetUp",
                                TAG
                            )
                            setupErrorMessage(R.string.sdk_error_unknown)
                            SdkStatus.ERROR
                        }

                        else -> {
                            logDebug("еvrotrustUserSetUpOnlineResult status OK", TAG)
                            SdkStatus.USER_SET_UP_ONLINE_READY
                        }
                    }
                }

                else -> {
                    logError("еvrotrustUserSetUpOnlineResult status null", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.ERROR
                }
            }
        )
    }

    override fun openDocument(
        activity: Activity,
        evrotrustTransactionId: String,
    ) {
        logDebug("openDocument transactionId: $evrotrustTransactionId", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openDocument pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("openDocument not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.OPEN_SINGLE_DOCUMENT
            )
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                pinCode.hashedPin
            )
            putExtra(
                EvrotrustConstants.EXTRA_TRANSACTION_ID,
                evrotrustTransactionId
            )
        }
        activity.startActivityForResult(
            intent,
            EvrotrustConstants.REQUEST_CODE_OPEN_SINGLE_DOCUMENT
        )
    }

    override fun openGroupOfDocuments(
        activity: Activity,
        evrotrustTransactionId: String,
    ) {
        logDebug("openGroupOfDocuments transactionId: $evrotrustTransactionId", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openGroupOfDocuments pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("openGroupOfDocuments not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.OPEN_GROUP_DOCUMENTS
            )
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                pinCode.hashedPin
            )
            putExtra(
                EvrotrustConstants.EXTRA_TRANSACTION_ID,
                evrotrustTransactionId
            )
        }
        activity.startActivityForResult(
            intent,
            EvrotrustConstants.REQUEST_CODE_OPEN_GROUP_DOCUMENTS
        )
    }

    override fun isSDKSetUp(): Boolean {
        val isSDKSetUp = EvrotrustSDK.getInstance(currentContext.get())
            .isSDKSetUp
        logDebug("isSDKSetUp: $isSDKSetUp", TAG)
        return isSDKSetUp
    }

    override fun changeSecurityContext(
        newSecurityContext: String,
    ) {
        logDebug("changeSecurityContext newSecurityContext: $newSecurityContext", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("changeSecurityContext pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("changeSecurityContext not isSDKSetUp", TAG)
            setupSdk()
        }
        if (pinCode.hashedPin == newSecurityContext) {
            logError("changeSecurityContext code == newSecurityContext", TAG)
            setAppStatus(SdkStatus.CHANGE_SECURITY_CONTEXT_READY)
            return
        }
        logDebug("changeSecurityContext oldSecurityContext: $pinCode", TAG)
        EvrotrustSDK.getInstance(currentContext.get())
            .changeSecurityContext(
                pinCode.hashedPin,
                newSecurityContext,
                currentContext.get(),
                this, // EvrotrustChangeSecurityContextResult
            )
    }

    override fun openSettingsScreens(
        activity: Activity,
    ) {
        logDebug("openSettingsScreens", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openSettingsScreens pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("openSettingsScreens not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.SETTINGS
            )
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                pinCode.hashedPin
            )
        }
        activity.startActivity(intent)
    }

    override fun openEditProfile(activity: Activity) {
        logDebug("openEditProfile", TAG)
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openEditProfile pinCode == null", TAG)
            setupErrorMessage(R.string.error_pin_code_not_setup)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("openEditProfile not isSDKSetUp", TAG)
            setupSdk()
        }
        val intent = Intent(activity, EvrotrustActivity::class.java).apply {
            putExtra(
                EvrotrustConstants.EXTRA_ACTIVITY_TYPE,
                EvrotrustConstants.EDIT_PERSONAL_DATA
            )
            putExtra(
                EvrotrustConstants.EXTRA_SECURITY_CONTEXT,
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
            )
        }
        activity.startActivityForResult(intent, EvrotrustConstants.REQUEST_CODE_EDIT_PERSONAL_DATA)
    }

    override fun onActivityResult(
        requestCode: Int,
        data: Intent?,
    ) {
        logDebug("setupSdkActivityResult requestCode: $requestCode", TAG)
        when (requestCode) {
            EvrotrustConstants.REQUEST_CODE_SETUP_PROFILE -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_SETUP_PROFILE", TAG)
                val result: EvrotrustSetupProfileResult? =
                    data?.getParcelableExtra(EvrotrustConstants.SETUP_PROFILE_RESULT)
                setAppStatus(
                    when (result?.status) {
                        EvrotrustConstants.ActivityStatus.OK -> {
                            if (result.isUserSetUp) {
                                logDebug("setupSdkActivityResult status OK result.isUserSetUp", TAG)
                                val oldUser = preferences.readUser()
                                val securityContext =
                                    if (result.securityContext.isNullOrEmpty()) oldUser?.securityContext
                                    else result.securityContext
                                val personalIdentificationNumber =
                                    if (result.personalIdentificationNumber.isNullOrEmpty()) oldUser?.personalIdentificationNumber
                                    else result.personalIdentificationNumber
                                val countryCode2 =
                                    if (result.countryCode2.isNullOrEmpty()) oldUser?.countryCode2
                                    else result.countryCode2
                                val countryCode3 =
                                    if (result.countryCode3.isNullOrEmpty()) oldUser?.countryCode3
                                    else result.countryCode3
                                val phone =
                                    if (result.phone.isNullOrEmpty()) oldUser?.phone
                                    else result.phone
                                val email = oldUser?.email
                                val firstName =
                                    if (result.firstName.isNullOrEmpty()) oldUser?.firstName
                                    else result.firstName
                                val middleName =
                                    if (result.middleName.isNullOrEmpty()) oldUser?.middleName
                                    else result.middleName
                                val lastName =
                                    if (result.lastName.isNullOrEmpty()) oldUser?.lastName
                                    else result.lastName
                                val firstLatinName =
                                    if (result.firstLatinName.isNullOrEmpty()) oldUser?.firstLatinName
                                    else result.firstLatinName
                                val middleLatinName =
                                    if (result.middleLatinName.isNullOrEmpty()) oldUser?.middleLatinName
                                    else result.middleLatinName
                                val lastLatinName =
                                    if (result.lastLatinName.isNullOrEmpty()) oldUser?.lastLatinName
                                    else result.lastLatinName
                                preferences.saveUser(
                                    UserModel(
                                        securityContext = securityContext,
                                        personalIdentificationNumber = personalIdentificationNumber,
                                        countryCode2 = countryCode2,
                                        countryCode3 = countryCode3,
                                        phone = phone,
                                        email = email,
                                        firstName = firstName,
                                        middleName = middleName,
                                        lastName = lastName,
                                        firstLatinName = firstLatinName,
                                        middleLatinName = middleLatinName,
                                        lastLatinName = lastLatinName,
                                        isIdentified = result.isIdentified,
                                        isSupervised = result.isSupervised,
                                        isReadyToSign = result.isReadyToSign,
                                        isRejected = result.isRejected,
                                    )
                                )
                                logDebug(
                                    "User:\n" +
                                            "securityContext: ${securityContext}\n" +
                                            "personalIdentificationNumber: ${personalIdentificationNumber}\n" +
                                            "countryCode2: ${countryCode2}\n" +
                                            "countryCode3: ${countryCode3}\n" +
                                            "email: $email\n" +
                                            "phone: $phone\n" +
                                            "firstName: ${firstName}\n" +
                                            "middleName: ${middleName}\n" +
                                            "lastName: ${lastName}\n" +
                                            "firstLatinName: ${firstLatinName}\n" +
                                            "middleLatinName: ${middleLatinName}\n" +
                                            "lastLatinName: ${lastLatinName}\n" +
                                            "isIdentified: ${result.isIdentified}\n" +
                                            "isSupervised: ${result.isSupervised}\n" +
                                            "isReadyToSign: ${result.isReadyToSign}\n" +
                                            "isRejected: ${result.isRejected}", TAG
                                )
                                SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_READY
                            } else {
                                logError(
                                    "setupSdkActivityResult status OK but not result.isUserSetUp",
                                    TAG
                                )
                                setupErrorMessage(R.string.sdk_error_user_not_specified)
                                SdkStatus.USER_SETUP_ERROR
                            }
                        }

                        EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                            logError("setupSdkActivityResult status ERROR_INPUT", TAG)
                            setupErrorMessage(R.string.sdk_error_wrong_input)
                            SdkStatus.USER_SETUP_ERROR
                        }

                        EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                            logError("setupSdkActivityResult status USER_CANCELED", TAG)
                            setupErrorMessage(R.string.sdk_error_cancelled)
                            SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_CANCELLED
                        }

                        EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                            logError("setupSdkActivityResult status USER_NOT_SET_UP", TAG)
                            setupErrorMessage(R.string.sdk_error_user_not_specified)
                            SdkStatus.USER_SETUP_ERROR
                        }

                        EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                            logError("setupSdkActivityResult status SDK_NOT_SET_UP", TAG)
                            setupErrorMessage(R.string.sdk_error_sdk_not_set)
                            setupSdk()
                            SdkStatus.SDK_SETUP_ERROR
                        }

                        null -> {
                            logError("setupSdkActivityResult status null", TAG)
                            setupErrorMessage(R.string.sdk_error_unknown)
                            SdkStatus.USER_SETUP_ERROR
                        }
                    }
                )
            }

            EvrotrustConstants.REQUEST_CODE_OPEN_SINGLE_DOCUMENT -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_OPEN_SINGLE_DOCUMENT", TAG)
                val result: EvrotrustOpenSingleDocumentResult? =
                    data?.getParcelableExtra(EvrotrustConstants.OPEN_SINGLE_DOCUMENT_RESULT)
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError("openDocumentActivityResult status USER_NOT_SET_UP", TAG)
                        setupErrorMessage(R.string.sdk_error_user_not_specified)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError("openDocumentActivityResult status SDK_NOT_SET_UP", TAG)
                        setupErrorMessage(R.string.sdk_error_sdk_not_set)
                        setupSdk()
                        setAppStatus(SdkStatus.SDK_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                        logError("openDocumentActivityResult status ERROR_INPUT", TAG)
                        setupErrorMessage(R.string.sdk_error_wrong_input)
                    }

                    EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                        logError("openDocumentActivityResult status USER_CANCELED", TAG)
//                        setupErrorMessage(R.string.sdk_error_cancelled)
                    }

                    EvrotrustConstants.ActivityStatus.OK -> {
                        logDebug("openDocumentActivityResult status OK", TAG)
                        var userDecision: UserDecision? = result.userDecision
                        setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY)
                    }

                    else -> {
                        logError("openDocumentActivityResult status null", TAG)
                        setupErrorMessage(R.string.sdk_error_unknown)
                    }
                }
            }

            EvrotrustConstants.REQUEST_CODE_OPEN_GROUP_DOCUMENTS -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_OPEN_GROUP_DOCUMENTS", TAG)
                val result: EvrotrustOpenGroupDocumentsResult? =
                    data?.getParcelableExtra(EvrotrustConstants.OPEN_GROUP_DOCUMENTS_RESULT);
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError(
                            "openGroupOfDocumentsActivityResult status USER_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_user_not_specified)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError(
                            "openGroupOfDocumentsActivityResult status SDK_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_sdk_not_set)
                        setupSdk()
                        setAppStatus(SdkStatus.SDK_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                        logError("openGroupOfDocumentsActivityResult status ERROR_INPUT", TAG)
                        setupErrorMessage(R.string.sdk_error_wrong_input)
                    }

                    EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                        logError("openGroupOfDocumentsActivityResult status USER_CANCELED", TAG)
                        setupErrorMessage(R.string.sdk_error_cancelled)
                    }

                    EvrotrustConstants.ActivityStatus.OK -> {
                        logDebug("openGroupOfDocumentsActivityResult status OK", TAG)
                        val userDecision: UserDecision = result.userDecision
                        setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_GROUP_DOCUMENTS_READY)
                    }

                    else -> {
                        logError("openGroupOfDocumentsActivityResult status null", TAG)
                        setupErrorMessage(R.string.sdk_error_unknown)
                    }
                }
            }

            EvrotrustConstants.REQUEST_CODE_EDIT_PERSONAL_DATA -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_EDIT_PERSONAL_DATA", TAG)
                val result: EvrotrustEditPersonalDataResult? =
                    data?.getParcelableExtra(EvrotrustConstants.EDIT_PERSONAL_DATA_RESULT)
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError(
                            "editPersonalDataActivityResult status USER_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_user_not_specified)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError(
                            "editPersonalDataActivityResult status SDK_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_sdk_not_set)
                        setupSdk()
                        setAppStatus(SdkStatus.SDK_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                        logError("editPersonalDataActivityResult status ERROR_INPUT", TAG)
                        setupErrorMessage(R.string.sdk_error_wrong_input)
                        setAppStatus(SdkStatus.ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                        logError("editPersonalDataActivityResult status USER_CANCELED", TAG)
                        setupErrorMessage(R.string.sdk_error_cancelled)
                        setAppStatus(SdkStatus.ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.OK -> {
                        logDebug("editPersonalDataActivityResult status OK", TAG)
                        val editPersonalData = result.isEditPersonalData
                        if (editPersonalData) {
                            val isIdentified = result.isIdentified
                            val isSupervised = result.isSupervised
                            val isReadyToSign = result.isReadyToSign
                            val isRejected = result.isRejected
                            val rejectReason = result.rejectReason
                            logDebug(
                                "editPersonalDataActivityResult isIdentified: $isIdentified isSupervised: $isSupervised isReadyToSign: $isReadyToSign isRejected: $isRejected rejectReason: ${rejectReason.name}",
                                TAG
                            )
                            setAppStatus(SdkStatus.ACTIVITY_RESULT_EDIT_PERSONAL_DATA_READY)
                        } else {
                            logError("editPersonalDataActivityResult status null", TAG)
                            setupErrorMessage(R.string.sdk_error_unknown)
                            setAppStatus(SdkStatus.ERROR)
                        }
                    }

                    else -> {
                        logError("editPersonalDataActivityResult status null", TAG)
                        setupErrorMessage(R.string.sdk_error_unknown)
                        setAppStatus(SdkStatus.ERROR)
                    }
                }
            }

            else -> {
                logError("onActivityResult wrong request code", TAG)
                setupErrorMessage(R.string.sdk_error_unknown)
            }
        }
    }

    override fun evrotrustChangeSecurityContextResult(
        result: EvrotrustChangeSecurityContextResult,
    ) {
        logDebug("evrotrustChangeSecurityContextResult result: ${result.status}", TAG)
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("evrotrustChangeSecurityContextResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_user_not_specified)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("evrotrustChangeSecurityContextResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.sdk_error_sdk_not_set)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                    logError("evrotrustChangeSecurityContextResult status ERROR_INPUT", TAG)
                    setupErrorMessage(R.string.sdk_error_wrong_input)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                    logError("evrotrustChangeSecurityContextResult status USER_CANCELED", TAG)
                    setupErrorMessage(R.string.sdk_error_cancelled)
                    SdkStatus.ERROR
                }

                EvrotrustConstants.ActivityStatus.OK -> {
                    if (!result.isSecurityContextChanged) {
                        logError(
                            "evrotrustChangeSecurityContextResult status OK but not isUserSetUp",
                            TAG
                        )
                        setupErrorMessage(R.string.sdk_error_unknown)
                        SdkStatus.ERROR
                    } else {
                        logDebug("evrotrustChangeSecurityContextResult status OK", TAG)
                        SdkStatus.CHANGE_SECURITY_CONTEXT_READY
                    }
                }

                else -> {
                    logError("evrotrustChangeSecurityContextResult status null", TAG)
                    setupErrorMessage(R.string.sdk_error_unknown)
                    SdkStatus.ERROR
                }
            }
        )
    }

    private fun setupErrorMessage(@StringRes errorMessageRes: Int) {
        logDebug("setupErrorMessage", TAG)
        this.errorMessageRes = errorMessageRes
        _errorMessageResLiveData.value = errorMessageRes
    }

    private fun setAppStatus(sdkStatus: SdkStatus) {
        logDebug("setAppStatus sdkStatus: $sdkStatus", TAG)
        _sdkStatusLiveData.value = sdkStatus
        if (sdkStatus == SdkStatus.SDK_SETUP_ERROR ||
            sdkStatus == SdkStatus.USER_SETUP_ERROR ||
            sdkStatus == SdkStatus.CRITICAL_ERROR
        ) {
            preferences.saveAppStatus(AppStatus.NOT_READY)
        }
    }


}