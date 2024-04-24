/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import com.digital.sofia.BuildConfig
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.logout.LogoutUseCase
import com.digital.sofia.domain.usecase.logs.UploadLogsUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogEvrotrustUtil
import com.digital.sofia.domain.utils.LogUtil
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.ActivitiesCommonHelper
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.ScreenshotsDetector
import com.digital.sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class MainViewModel(
    private val rootBeer: RootBeer,
    private val loginTimer: LoginTimer,
    private val screenshotsDetector: ScreenshotsDetector,
    private val authorizationHelper: AuthorizationHelper,
    private val activitiesCommonHelper: ActivitiesCommonHelper,
    private val uploadLogsUseCase: UploadLogsUseCase,
    private val preferences: PreferencesRepository,
    appEventsHelper: AppEventsHelper,
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
        private const val TAG = "MainViewModelTag"
        private const val LOG_APP_FOLDER_NAME = "DigitalSofia"
        private const val EVROTRUST_LOG_DIRECTORY = "Evrotrust"
    }

    override val isAuthorizationActive: Boolean = false

    fun getStartDestination(intent: Intent): StartDestination {
        return StartDestination(R.id.startFlowFragment)
    }

    fun onSaveInstanceState(bundle: Bundle) {
        // TODO
    }

    fun onRestoreInstanceState(bundle: Bundle) {
        // TODO
    }

    fun onPasswordRequiredAuthError() {
        // TODO
    }

    fun onProfileBlockedError() {
        // TODO
    }

    fun onForceUpdateError() {
        // TODO
    }

    fun onRootDetectedError() {
        // TODO
    }

    fun logout(finishCallback: (() -> Unit)?) {
        // TODO
    }

    override fun onFirstAttach() {
        super.onFirstAttach()
        logDebug("onFirstAttach", TAG)
        activitiesCommonHelper.getFcmToken()
        checkInternalRoot()
        uploadEvrotrustLogs()
    }

    fun applyLightDarkTheme() {
        activitiesCommonHelper.applyLightDarkTheme()
    }

    fun onResume(activity: Activity) {
        loginTimer.activityOnResume()
    }

    fun onPause(activity: Activity) {
        loginTimer.activityOnPause()
        screenshotsDetector.stopDetecting(activity)
    }

    fun onDestroy() {
        authorizationHelper.stopUpdateTokenTimer()
        loginTimer.activityOnDestroy()
    }

    fun dispatchTouchEvent() {
        loginTimer.dispatchTouchEvent()
    }

    override fun onBackPressed() {
        if (!findActivityNavController().popBackStack()) {
            closeActivity()
        }
    }

    private fun checkInternalRoot() {
        if (!BuildConfig.DEBUG) {
            viewModelScope.launch(Dispatchers.IO) {
                if (rootBeer.isRooted) {
                    logout()
                }
            }
        }
    }

    private fun uploadEvrotrustLogs() {
        val user = preferences.readUser()
        if (user != null && !user.personalIdentificationNumber.isNullOrEmpty()) {
            val filesToUpload = getFilesToUpload()
            filesToUpload?.let { files ->
                if (files.isNotEmpty()) {
                    uploadLogsUseCase.invoke(
                        personalIdentifier = user.personalIdentificationNumber ?: "",
                        files = files
                    ).onEach { result ->
                        result.onLoading {
                            logDebug("uploadEvrotrustLogs onLoading", TAG)
                        }.onSuccess {
                            logDebug("uploadEvrotrustLogs onSuccess", TAG)
                            files.forEach {
                                it.delete()
                            }
                        }.onRetry {
                            uploadEvrotrustLogs()
                        }.onFailure {
                            logError("uploadEvrotrustLogs onFailure", it, TAG)
                        }
                    }.launchInScope(viewModelScope)
                }
            }
        }
    }

    private fun getFilesToUpload(): List<File>? {
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .path
        val appDirectory = File(path, LOG_APP_FOLDER_NAME)
        val evrotrustLogsDirectory = File(appDirectory, EVROTRUST_LOG_DIRECTORY)
        val files = evrotrustLogsDirectory.listFiles()?.filter { file ->
            file.lastModified() < Date().time
        }
        return files
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        stopListenNetworkState()
    }

}