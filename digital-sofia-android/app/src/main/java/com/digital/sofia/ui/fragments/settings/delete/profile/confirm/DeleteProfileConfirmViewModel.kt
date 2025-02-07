package com.digital.sofia.ui.fragments.settings.delete.profile.confirm

import androidx.lifecycle.viewModelScope
import com.digital.sofia.R
import com.digital.sofia.domain.models.base.onFailure
import com.digital.sofia.domain.models.base.onLoading
import com.digital.sofia.domain.models.base.onRetry
import com.digital.sofia.domain.models.base.onSuccess
import com.digital.sofia.domain.repository.common.CryptographyRepository
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.usecase.firebase.UpdateFirebaseTokenUseCase
import com.digital.sofia.domain.usecase.user.DeleteUserUseCase
import com.digital.sofia.domain.usecase.user.GetLogLevelUseCase
import com.digital.sofia.domain.utils.AuthorizationHelper
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.models.common.Message
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.NetworkConnectionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach

class DeleteProfileConfirmViewModel(
    private val deleteUserUseCase: DeleteUserUseCase,
    loginTimer: LoginTimer,
    appEventsHelper: AppEventsHelper,
    preferences: PreferencesRepository,
    authorizationHelper: AuthorizationHelper,
    localizationManager: LocalizationManager,
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
    cryptographyRepository = cryptographyRepository,
    updateFirebaseTokenUseCase = updateFirebaseTokenUseCase,
    getLogLevelUseCase = getLogLevelUseCase,
    networkConnectionManager = networkConnectionManager,
    firebaseMessagingServiceHelper = firebaseMessagingServiceHelper,
) {

    companion object {
        private const val TAG = "DeleteProfileConfirmViewModelTag"
    }

    override val isAuthorizationActive: Boolean = true

    fun onDeleteClicked() {
        logDebug("onDeleteClicked", TAG)
        deleteUser()
    }

    private fun deleteUser() {
        deleteUserUseCase.invoke().onEach { result ->
            result.onLoading {
                logDebug("deleteUser onLoading", TAG)
                showLoader()
            }.onSuccess {
                logDebug("deleteUser onSuccess", TAG)
                hideLoader()
                hideErrorState()
                toRegistrationFragment()
            }.onRetry {
                deleteUser()
            }.onFailure { failure ->
                logError("deleteUser onFailure", failure, TAG)
                when (failure.responseCode) {
                    409 -> showMessage(Message.error(R.string.profile_delete_error_description))
                    else -> showMessage(Message.error(R.string.error_server_error))
                }
                hideLoader()
                hideErrorState()
            }
        }.launchInScope(viewModelScope)

    }

}