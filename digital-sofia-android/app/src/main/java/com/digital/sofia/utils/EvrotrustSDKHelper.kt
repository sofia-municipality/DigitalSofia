/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import com.digital.sofia.BuildConfig
import com.digital.sofia.R
import com.digital.sofia.data.BuildConfig.EVROTRUST_APPLICATION_NUMBER
import com.digital.sofia.data.BuildConfig.IS_EVROTRUST_PROD
import com.digital.sofia.domain.models.common.AppStatus
import com.digital.sofia.domain.models.common.SdkStatus
import com.digital.sofia.domain.models.user.UserModel
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogEvrotrustUtil.logMessage
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.readOnly
import com.digital.sofia.extensions.setValueOnMainThread
import com.evrotrust.lib.EvrotrustSDK
import com.evrotrust.lib.activities.EvrotrustActivity
import com.evrotrust.lib.listeners.EvrotrustChangeSecurityContextListener
import com.evrotrust.lib.listeners.EvrotrustCheckUserStatusListener
import com.evrotrust.lib.listeners.EvrotrustSetupSDKListener
import com.evrotrust.lib.listeners.EvrotrustUserSetUpOnlineListener
import com.evrotrust.lib.models.EvrotrustChangeSecurityContextResult
import com.evrotrust.lib.models.EvrotrustCheckUserStatusResult
import com.evrotrust.lib.models.EvrotrustEditPersonalDataResult
import com.evrotrust.lib.models.EvrotrustOpenGroupDocumentsResult
import com.evrotrust.lib.models.EvrotrustOpenSingleDocumentResult
import com.evrotrust.lib.models.EvrotrustSetupProfileResult
import com.evrotrust.lib.models.EvrotrustSetupSDKResult
import com.evrotrust.lib.models.EvrotrustUserSetUpOnlineResult
import com.evrotrust.lib.services.EvrotrustCustomization
import com.evrotrust.lib.utils.EvrotrustConstants
import com.evrotrust.lib.utils.EvrotrustConstants.UserDecision

interface EvrotrustSDKHelper {

    companion object {
        private const val TAG = "EvrotrustSDKHelperTag"
    }

    val sdkStatusLiveData: LiveData<SdkStatus>

    val errorMessageResLiveData: LiveData<Int>

    var errorMessageRes: Int?

    // actions

    fun setupSdk()

    fun startSetupUserActivity(
        activity: Activity,
        prefillPersonalIdentificationNumber: Boolean,
    )

    fun checkUserStatus()

    fun openDocument(
        activity: Activity,
        evrotrustTransactionId: String,
    )

    fun changeSecurityContext(
        hashedPin: String,
    )

    fun openSettingsScreens(
        activity: Activity,
    )

    fun openEditProfile(
        activity: Activity,
    )

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
    EvrotrustUserSetUpOnlineListener {

    companion object {
        private const val TAG = "EvrotrustSDKHelperTag"
    }

    override var errorMessageRes: Int? = null

    private val _sdkStatusLiveData = SingleLiveEvent<SdkStatus>()
    override val sdkStatusLiveData = _sdkStatusLiveData.readOnly()

    private val _errorMessageResLiveData = SingleLiveEvent<Int>()
    override val errorMessageResLiveData = _errorMessageResLiveData.readOnly()

    private var shouldHandleRequestCode = false

    override fun setupSdk() {
        logDebug("setupSdk", TAG)
        val user = preferences.readUser()
        logMessage("Started Evrotrust SDK setup for user with personal identification number ${user?.personalIdentificationNumber}")
        val environment = if (IS_EVROTRUST_PROD) EvrotrustConstants.ENVIRONMENT_PROD else EvrotrustConstants.ENVIRONMENT_TEST
        EvrotrustSDK.getInstance(currentContext.get())
            .setupSDK(
                EVROTRUST_APPLICATION_NUMBER,
                currentContext.get(),
                environment,
                this // EvrotrustSetupSDKResult
            )
        val customization = EvrotrustCustomization().apply {
            imageCustomization.questionsTitleImage = R.drawable.img_logo_et
            imageCustomization.contactsTitleImage = R.drawable.img_logo_et
            imageCustomization.documentsTitleImage = R.drawable.img_logo_et
            imageCustomization.scanInstructionsImage = R.drawable.img_logo_scan_et
            imageCustomization.isWhiteEvrotrustLogo = false
            nightMode = EvrotrustConstants.NightMode.MODE_NIGHT_NO
        }
        EvrotrustSDK.getInstance(currentContext.get())
            .setCustomization(customization)
    }

    override fun evrotrustSetupSDKResult(
        result: EvrotrustSetupSDKResult,
    ) {
        logDebug("evrotrustSetupSDKResult result: ${result.status}", TAG)
        logMessage(
            "Setup Evrotrust SDK returned the following result:\n" +
                    "Status: ${result.status.name}\n" +
                    "IsSetUp: ${result.isSetUp}\n" +
                    "IsInMaintenance: ${result.isInMaintenance}"
        )
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("evrotrustSetupSDKResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
                    setupSdk()
                    SdkStatus.SDK_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("evrotrustSetupSDKResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
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
                        setupErrorMessage(R.string.error_common)
                        SdkStatus.SDK_SETUP_ERROR
                    } else {
                        logDebug("setupSdkResult status OK", TAG)
                        SdkStatus.SDK_SETUP_READY
                    }
                }

                else -> {
                    logError("evrotrustSetupSDKResult status else", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }

    override fun startSetupUserActivity(
        activity: Activity,
        prefillPersonalIdentificationNumber: Boolean,
    ) {
        logDebug("startSetupUserActivity", TAG)
        logMessage("Starting Evrotrust SDK user setup activity for user with personal identification number: $prefillPersonalIdentificationNumber")
        val pinCode = preferences.readPinCode()
        if (pinCode == null) {
            logError("startSetupUserActivity pinCode == nul", TAG)
            setupErrorMessage(R.string.error_common)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!pinCode.validate()) {
            logError("startSetupUserActivity !pinCode.validate()", TAG)
            setupErrorMessage(R.string.error_common)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        var user: UserModel? = null
        if (prefillPersonalIdentificationNumber) {
            user = preferences.readUser()
            if (user == null) {
                logError("startSetupUserActivity user == null", TAG)
                setupErrorMessage(R.string.error_common)
                setAppStatus(SdkStatus.CRITICAL_ERROR)
                return
            }
            if (user.personalIdentificationNumber.isNullOrEmpty()) {
                logError(
                    "startSetupUserActivity user.personalIdentificationNumber.isNullOrEmpty()",
                    TAG
                )
                setupErrorMessage(R.string.error_common)
                setAppStatus(SdkStatus.CRITICAL_ERROR)
                return
            }
        }
        if (!isSDKSetUp()) {
            logError("startSetupUserActivity not isSDKSetUp", TAG)
            setupSdk()
        }
        preferences.saveAppStatus(AppStatus.NOT_REGISTERED)
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
            if (prefillPersonalIdentificationNumber) {
                putExtra(
                    EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_TYPE,
                    EvrotrustConstants.USER_TYPE_IDENTIFICATION_NUMBER
                )
                putExtra(
                    EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_DATA_VALUE,
                    user?.personalIdentificationNumber
                )
                putExtra(
                    EvrotrustConstants.EXTRA_USER_INFORMATION_FOR_CHECK_COUNTRY_CODE3,
                    "BGR"
                )
            }
        }
        shouldHandleRequestCode = true
        activity.startActivityForResult(
            intent,
            EvrotrustConstants.REQUEST_CODE_SETUP_PROFILE,
        )
    }

    override fun checkUserStatus() {
        logDebug("checkUserStatus", TAG)
        val user = preferences.readUser()
        logMessage("Starting Evrotrust SDK check user for user with personal identification number: ${user?.personalIdentificationNumber}")
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


    override fun еvrotrustCheckUserStatusResult(
        result: EvrotrustCheckUserStatusResult
    ) {
        logDebug("еvrotrustCheckUserStatusResult result: ${result.status}", TAG)
        logMessage(
            "Check user Evrotrust SDK returned the following result:\n" +
                    "Status: ${result.status.name}\n" +
                    "IsIdentified: ${result.isIdentified}\n" +
                    "IsSuccessfulCheck: ${result.isSuccessfulCheck}" +
                    "IsReadyToSign: ${result.isReadyToSign}" +
                    "IsInMaintenance: ${result.isRejected}" +
                    "IsSupervised: ${result.isSupervised}" +
                    "RejectReason: ${result.rejectReason?.name}"
        )
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("еvrotrustCheckUserStatusResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("еvrotrustCheckUserStatusResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
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
                        setupErrorMessage(R.string.error_common)
                        SdkStatus.USER_SETUP_ERROR
                    } else {
                        val isIdentified = result.isIdentified
                        val isSupervised = result.isSupervised
                        val isReadyToSign = result.isReadyToSign
                        val isRejected = result.isRejected
                        val hasConfirmedPhone = result.hasConfirmedPhone()
                        val hasConfirmedEmail = result.hasConfirmedEmail()
                        logDebug(
                            "checkUserStatusResult status OK isIdentified: $isIdentified isSupervised: $isSupervised isReadyToSign: $isReadyToSign isRejected: $isRejected hasConfirmedPhone: $hasConfirmedPhone hasConfirmedEmail: $hasConfirmedEmail",
                            TAG
                        )
                        if (isIdentified) {
                            SdkStatus.USER_STATUS_READY
                        } else {
                            SdkStatus.USER_STATUS_NOT_IDENTIFIED
                        }
                    }
                }

                else -> {
                    logError("еvrotrustCheckUserStatusResult status null", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.USER_SETUP_ERROR
                }
            }
        )
    }


    override fun еvrotrustUserSetUpOnlineResult(
        result: EvrotrustUserSetUpOnlineResult,
    ) {
        logDebug("еvrotrustUserSetUpOnlineResult result: ${result.status}", TAG)
        logMessage(
            "Setup user online Evrotrust SDK returned the following result:\n" +
                    "Status: ${result.status.name}\n" +
                    "IsUserSetUp: ${result.isUserSetUp}\n" +
                    "IsSuccessfulCheck: ${result.isSuccessfulCheck}"
        )
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpOnlineResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("еvrotrustUserSetUpOnlineResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
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
                            setupErrorMessage(R.string.error_common)
                            SdkStatus.USER_SETUP_ERROR
                        }

                        !result.isUserSetUp -> {
                            logError(
                                "еvrotrustUserSetUpOnlineResult !result.isUserSetUp",
                                TAG
                            )
                            setupErrorMessage(R.string.error_common)
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
                    setupErrorMessage(R.string.error_common)
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
        logMessage("Starting Evrotrust SDK open document with transaction id: $evrotrustTransactionId")
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openDocument pinCode == null", TAG)
            setupErrorMessage(R.string.error_common)
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
        shouldHandleRequestCode = true
        activity.startActivityForResult(
            intent,
            EvrotrustConstants.REQUEST_CODE_OPEN_SINGLE_DOCUMENT
        )
    }


    private fun isSDKSetUp(): Boolean {
        val isSDKSetUp = EvrotrustSDK.getInstance(currentContext.get())
            .isSDKSetUp
        logDebug("isSDKSetUp: $isSDKSetUp", TAG)
        return isSDKSetUp
    }

    override fun changeSecurityContext(
        hashedPin: String,
    ) {
        logDebug("changeSecurityContext newSecurityContext: $hashedPin", TAG)
        logMessage("Starting Evrotrust SDK change security context")
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("changeSecurityContext pinCode == null", TAG)
            setupErrorMessage(R.string.error_common)
            setAppStatus(SdkStatus.CRITICAL_ERROR)
            return
        }
        if (!isSDKSetUp()) {
            logError("changeSecurityContext not isSDKSetUp", TAG)
            setupSdk()
        }
        if (pinCode.hashedPin == hashedPin) {
            logError("changeSecurityContext code == newSecurityContext", TAG)
            setAppStatus(SdkStatus.CHANGE_SECURITY_CONTEXT_READY)
            return
        }
        logDebug("changeSecurityContext old: ${pinCode.hashedPin}\nnew:$hashedPin", TAG)
        EvrotrustSDK.getInstance(currentContext.get())
            .changeSecurityContext(
                pinCode.hashedPin,
                hashedPin,
                currentContext.get(),
                this, // EvrotrustChangeSecurityContextResult
            )
    }

    override fun openSettingsScreens(
        activity: Activity,
    ) {
        logDebug("openSettingsScreens", TAG)
        logMessage("Starting Evrotrust SDK open settings screen.")
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openSettingsScreens pinCode == null", TAG)
            setupErrorMessage(R.string.error_common)
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
        shouldHandleRequestCode = true
        activity.startActivity(intent)
    }

    override fun openEditProfile(activity: Activity) {
        logDebug("openEditProfile", TAG)
        val user = preferences.readUser()
        logMessage("Starting Evrotrust SDK edit profile for user with personal identification number: ${user?.personalIdentificationNumber}")
        val pinCode = preferences.readPinCode()
        if (pinCode == null || !pinCode.validate()) {
            logError("openEditProfile pinCode == null", TAG)
            setupErrorMessage(R.string.error_common)
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
                pinCode.hashedPin
            )
        }
        shouldHandleRequestCode = true
        activity.startActivityForResult(intent, EvrotrustConstants.REQUEST_CODE_EDIT_PERSONAL_DATA)
    }

    override fun onActivityResult(
        requestCode: Int,
        data: Intent?,
    ) {
        logDebug(
            "setupSdkActivityResult requestCode: $requestCode shouldHandleRequestCode: $shouldHandleRequestCode",
            TAG
        )
        if (!shouldHandleRequestCode) return
        shouldHandleRequestCode = false
        when (requestCode) {
            EvrotrustConstants.REQUEST_CODE_SETUP_PROFILE -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_SETUP_PROFILE", TAG)
                val result: EvrotrustSetupProfileResult? =
                    data?.getParcelableExtra(EvrotrustConstants.SETUP_PROFILE_RESULT)
                logMessage(
                    "Setup user online Evrotrust SDK returned the following result:\n" +
                            "Status: ${result?.status?.name}\n" +
                            "SecurityContext: ${result?.securityContext}\n" +
                            "PersonalIdentificationNumber: ${result?.personalIdentificationNumber}\n" +
                            "CountryCode2: ${result?.countryCode2}\n" +
                            "CountryCode3: ${result?.countryCode3}\n" +
                            "Phone: ${result?.phone}\n" +
                            "FirstName: ${result?.firstName}\n" +
                            "MiddleName: ${result?.middleName}\n" +
                            "LastName: ${result?.lastName}\n" +
                            "FirstLatinName: ${result?.firstLatinName}\n" +
                            "MiddleLatinName: ${result?.middleLatinName}\n" +
                            "LastLatinName: ${result?.lastLatinName}\n" +
                            "IsIdentified: ${result?.isIdentified}\n" +
                            "IsSupervised: ${result?.isSupervised}\n" +
                            "IsReadyToSign: ${result?.isReadyToSign}\n" +
                            "IsRejected: ${result?.isRejected}"
                )
                logError("setupSdkActivityResult result: ${result?.status?.name}", TAG)
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
                                val isIdentified = result.isIdentified
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
                                        isIdentified = isIdentified,
                                        isSupervised = result.isSupervised,
                                        isReadyToSign = result.isReadyToSign,
                                        isRejected = result.isRejected,
                                        isVerified = oldUser?.isVerified,
                                        isDebug = oldUser?.isDebug
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
                                            "isRejected: ${result.isRejected}\n" +
                                            "isVerified: ${oldUser?.isVerified}", TAG
                                )
                                if (isIdentified) {
                                    SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_READY
                                } else {
                                    SdkStatus.USER_STATUS_NOT_IDENTIFIED
                                }
                            } else {
                                logError(
                                    "setupSdkActivityResult status OK but not result.isUserSetUp",
                                    TAG
                                )
                                setupErrorMessage(R.string.error_common)
                                SdkStatus.ACTIVITY_RESULT_SETUP_PROFILE_ERROR
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
                            setupErrorMessage(R.string.error_common)
                            SdkStatus.USER_SETUP_ERROR
                        }

                        EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                            logError("setupSdkActivityResult status SDK_NOT_SET_UP", TAG)
                            setupErrorMessage(R.string.error_common)
                            setupSdk()
                            SdkStatus.SDK_SETUP_ERROR
                        }

                        null -> {
                            logError("setupSdkActivityResult status null", TAG)
                            setupErrorMessage(R.string.error_common)
                            SdkStatus.USER_SETUP_ERROR
                        }
                    }
                )
            }

            EvrotrustConstants.REQUEST_CODE_OPEN_SINGLE_DOCUMENT -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_OPEN_SINGLE_DOCUMENT", TAG)
                val result: EvrotrustOpenSingleDocumentResult? =
                    data?.getParcelableExtra(EvrotrustConstants.OPEN_SINGLE_DOCUMENT_RESULT)
                logMessage(
                    "Opening single document Evrotrust SDK returned the following result:\n" +
                            "Status: ${result?.status?.name}\n" +
                            "UserDecision: ${result?.userDecision}"
                )
                logError("openDocumentActivityResult result: ${result?.status?.name}", TAG)
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError("openDocumentActivityResult status USER_NOT_SET_UP", TAG)
                        setupErrorMessage(R.string.error_common)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError("openDocumentActivityResult status SDK_NOT_SET_UP", TAG)
                        setupErrorMessage(R.string.error_common)
                        setupSdk()
                        setAppStatus(SdkStatus.SDK_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.ERROR_INPUT -> {
                        logError("openDocumentActivityResult status ERROR_INPUT", TAG)
                        setupErrorMessage(R.string.sdk_error_wrong_input)
                    }

                    EvrotrustConstants.ActivityStatus.USER_CANCELED -> {
                        logError("openDocumentActivityResult status USER_CANCELED", TAG)
                        setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_CANCELLED)
                    }

                    EvrotrustConstants.ActivityStatus.OK -> {
                        when (result.userDecision) {
                            UserDecision.REJECTED -> {
                                logError(
                                    "openDocumentActivityResult status OK but userDecision.REJECTED",
                                    TAG
                                )
                                setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_REJECTED)
                            }

                            UserDecision.APPROVED -> {
                                logDebug(
                                    "openDocumentActivityResult status OK and userDecision.APPROVED",
                                    TAG
                                )
                                setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_READY)
                            }

                            else -> {
                                logError(
                                    "openDocumentActivityResult status OK but userDecision else",
                                    TAG
                                )
                                setAppStatus(SdkStatus.ACTIVITY_RESULT_OPEN_SINGLE_DOCUMENT_CANCELLED)
                            }
                        }

                    }

                    else -> {
                        logError("openDocumentActivityResult status null", TAG)
                        setupErrorMessage(R.string.error_common)
                    }
                }
            }

            EvrotrustConstants.REQUEST_CODE_OPEN_GROUP_DOCUMENTS -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_OPEN_GROUP_DOCUMENTS", TAG)
                val result: EvrotrustOpenGroupDocumentsResult? =
                    data?.getParcelableExtra(EvrotrustConstants.OPEN_GROUP_DOCUMENTS_RESULT)
                logMessage(
                    "Opening group documents Evrotrust SDK returned the following result:\n" +
                            "Status: ${result?.status?.name}\n" +
                            "UserDecision: ${result?.userDecision}"
                )
                logError("setupSdkActivityResult result: ${result?.status?.name}", TAG)
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError(
                            "openGroupOfDocumentsActivityResult status USER_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.error_common)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError(
                            "openGroupOfDocumentsActivityResult status SDK_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.error_common)
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
                        setupErrorMessage(R.string.error_common)
                    }
                }
            }

            EvrotrustConstants.REQUEST_CODE_EDIT_PERSONAL_DATA -> {
                logDebug("setupSdkActivityResult REQUEST_CODE_EDIT_PERSONAL_DATA", TAG)
                val result: EvrotrustEditPersonalDataResult? =
                    data?.getParcelableExtra(EvrotrustConstants.EDIT_PERSONAL_DATA_RESULT)
                logMessage(
                    "Editing personal data Evrotrust SDK returned the following result:\n" +
                            "Status: ${result?.status?.name}\n" +
                            "IsIdentified: ${result?.isIdentified}\n" +
                            "IsEditPersonalData: ${result?.isEditPersonalData}" +
                            "IsReadyToSign: ${result?.isReadyToSign}" +
                            "IsInMaintenance: ${result?.isRejected}" +
                            "IsSupervised: ${result?.isSupervised}" +
                            "RejectReason: ${result?.rejectReason?.name}"
                )
                logError("setupSdkActivityResult result: ${result?.status?.name}", TAG)
                when (result?.status) {
                    EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                        logError(
                            "editPersonalDataActivityResult status USER_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.error_common)
                        setAppStatus(SdkStatus.USER_SETUP_ERROR)
                    }

                    EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                        logError(
                            "editPersonalDataActivityResult status SDK_NOT_SET_UP",
                            TAG
                        )
                        setupErrorMessage(R.string.error_common)
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
                                "editPersonalDataActivityResult isIdentified: $isIdentified isSupervised: $isSupervised isReadyToSign: $isReadyToSign isRejected: $isRejected rejectReason: ${rejectReason?.name}",
                                TAG
                            )
                            if (isIdentified) {
                                setAppStatus(SdkStatus.ACTIVITY_RESULT_EDIT_PERSONAL_DATA_READY)
                            } else {
                                setAppStatus(SdkStatus.USER_STATUS_NOT_IDENTIFIED)
                            }
                        } else {
                            logError("editPersonalDataActivityResult status null", TAG)
                            setupErrorMessage(R.string.error_common)
                            setAppStatus(SdkStatus.ERROR)
                        }
                    }

                    else -> {
                        logError("editPersonalDataActivityResult status null", TAG)
                        setupErrorMessage(R.string.error_common)
                        setAppStatus(SdkStatus.ERROR)
                    }
                }
            }

            else -> {
                logError("onActivityResult wrong request code", TAG)
                setupErrorMessage(R.string.error_common)
            }
        }
    }

    override fun evrotrustChangeSecurityContextResult(
        result: EvrotrustChangeSecurityContextResult,
    ) {
        logDebug("evrotrustChangeSecurityContextResult result: ${result.status}", TAG)
        logMessage(
            "Changing security context Evrotrust SDK returned the following result:\n" +
                    "Status: ${result.status.name}\n" +
                    "IsSecurityContextChanged: ${result.isSecurityContextChanged}"
        )
        setAppStatus(
            when (result.status) {
                EvrotrustConstants.ActivityStatus.USER_NOT_SET_UP -> {
                    logError("evrotrustChangeSecurityContextResult status USER_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.USER_SETUP_ERROR
                }

                EvrotrustConstants.ActivityStatus.SDK_NOT_SET_UP -> {
                    logError("evrotrustChangeSecurityContextResult status SDK_NOT_SET_UP", TAG)
                    setupErrorMessage(R.string.error_common)
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
                        setupErrorMessage(R.string.error_common)
                        SdkStatus.ERROR
                    } else {
                        logDebug("evrotrustChangeSecurityContextResult status OK", TAG)
                        SdkStatus.CHANGE_SECURITY_CONTEXT_READY
                    }
                }

                else -> {
                    logError("evrotrustChangeSecurityContextResult status null", TAG)
                    setupErrorMessage(R.string.error_common)
                    SdkStatus.ERROR
                }
            }
        )
    }

    private fun setupErrorMessage(@StringRes errorMessageRes: Int) {
        logDebug("setupErrorMessage", TAG)
        this.errorMessageRes = errorMessageRes
        _errorMessageResLiveData.setValueOnMainThread(errorMessageRes)
    }

    private fun setAppStatus(sdkStatus: SdkStatus) {
        logDebug("setAppStatus sdkStatus: $sdkStatus", TAG)
        _sdkStatusLiveData.setValueOnMainThread(sdkStatus)
        if (sdkStatus == SdkStatus.SDK_SETUP_ERROR ||
            sdkStatus == SdkStatus.USER_SETUP_ERROR ||
            sdkStatus == SdkStatus.CRITICAL_ERROR
        ) {
            preferences.saveAppStatus(AppStatus.NOT_REGISTERED)
        }
    }

    private fun logMessage(message: String) {
        val user = preferences.readUser()
        if (user?.isDebug == true) {
            logMessage(
                message = message,
                tag = TAG,
            )
        }
    }
}