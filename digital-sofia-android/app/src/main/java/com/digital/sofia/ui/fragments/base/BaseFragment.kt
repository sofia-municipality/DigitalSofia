/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.ui.fragments.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.digital.sofia.R
import com.digital.sofia.databinding.LayoutAppMenuBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.findActivityNavController
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.extensions.onClickThrottle
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.models.common.BackFragment
import com.digital.sofia.models.common.ErrorState
import com.digital.sofia.models.common.LoadingState
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.MessageBannerHolder
import com.digital.sofia.models.common.NetworkState
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.models.common.UiState
import com.digital.sofia.models.common.isConnected
import com.digital.sofia.ui.BaseViewModel
import com.digital.sofia.ui.activity.MainActivity
import com.digital.sofia.ui.view.ComplexGestureRefreshView
import com.digital.sofia.utils.AlertDialogResultListener
import kotlinx.coroutines.flow.onEach
import kotlin.properties.Delegates

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment(),
    BackFragment,
    MessageBannerHolder,
    AlertDialogResultListener {

    companion object {
        private const val TAG = "BaseFragmentTag"
    }

    abstract val viewModel: VM

    private var viewBinding: VB? = null

    private var shouldRefresh: Boolean by Delegates.observable(true) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue) {
            viewModel.refreshData()
            refreshScreen()
        }
    }

    private var popupWindow: PopupWindow? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    val binding get() = viewBinding!!

    abstract fun getViewBinding(): VB

    open val shouldKeepBinding = false

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = getViewBinding()
        return viewBinding?.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavControllers()
        subscribeToBaseLiveData()
        super.onViewCreated(view, savedInstanceState)
        viewModel.attachView()
        (activity as? MainActivity)?.alertDialogResultListener = this
        onCreated()
        onCreated(savedInstanceState)
        setupView()
        setupControls()
        subscribeToLiveData()
        viewModel.checkNetworkConnection()
    }

    protected open fun onCreated(savedInstanceState: Bundle?) {
        // Override when needed
    }

    protected open fun onCreated() {
        // Override when needed
    }

    protected open fun setupView() {
        // Override when needed
    }

    protected open fun setupControls() {
        // Override when needed
    }

    protected open fun subscribeToLiveData() {
        // Override when needed
    }

    override fun onAlertDialogResult(result: AlertDialogResult) {
        // Override when needed
    }

    protected open fun setupNavControllers() {
        setupActivityNavController()
        // Search for the flow controller
        viewModel.bindFlowNavController(findNavController())
    }

    @CallSuper
    protected fun setupActivityNavController() {
        // Search for the activity controller
        viewModel.bindActivityNavController(findActivityNavController())
    }

    private fun subscribeToBaseLiveData() {
        viewModel.closeActivityLiveData.observe(viewLifecycleOwner) {
            activity?.finish()
        }
        viewModel.backPressedFailedLiveData.observe(viewLifecycleOwner) {
            try {
                ((parentFragment as? NavHostFragment)?.parentFragment as? BaseFlowFragment<*, *>)?.onExit()
            } catch (e: Exception) {
                logError("backPressedFailedLiveData Exception: ${e.message}", e, TAG)
            }
        }
        viewModel.showMessageLiveData.observe(viewLifecycleOwner) {
            showMessage(it)
        }
        viewModel.uiStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Ready -> {
                    hideEmptyState()
                    hideErrorState()
                }
                is UiState.Empty -> showEmptyState()
            }
        }
        viewModel.loadingStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is LoadingState.Ready -> hideLoader()
                is LoadingState.Loading -> showLoader(it.message)
            }
        }
        viewModel.errorStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ErrorState.Ready -> hideErrorState()
                is ErrorState.Error -> showErrorState(
                    title = it.title,
                    iconRes = it.iconRes,
                    showIcon = it.showIcon,
                    showTitle = it.showTitle,
                    description = it.description,
                    showDescription = it.showDescription,
                    showActionOneButton = it.showActionTwoButton,
                    showActionTwoButton = it.showActionTwoButton,
                    actionOneButtonText = it.actionOneButtonText,
                    actionTwoButtonText = it.actionTwoButtonText,
                )
            }
        }
        viewModel.showBetaStateLiveData.observe(viewLifecycleOwner) {
            (activity as? MessageBannerHolder)?.showBeta()
        }
        viewModel.networkStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Disconnected -> showNoInternetConnectionState()
                is NetworkState.Connected -> hideNoInternetConnectionState()
            }
        }
        viewModel.newAppEventLiveData.observe(viewLifecycleOwner) { notification ->
            viewModel.checkAppEvents(notificationModel = notification)
        }
        viewModel.newFirebaseMessageLiveData.observe(viewLifecycleOwner) {
            viewModel.onNewFirebaseMessage(it)
        }
        viewModel.newTokenEventLiveData.observe(viewLifecycleOwner) {
            logDebug("firebaseMessagingServiceHelper newTokenEventLive", TAG)
            viewModel.onNewTokenEvent()
        }
        viewModel.logoutUserEventLiveData.observe(viewLifecycleOwner) {
            logDebug("authorizationHelper logoutUserEvent", TAG)
            viewModel.toRegistrationFragment()
        }
        viewModel.newNetworkConnectionChangeEventLiveData.observe(viewLifecycleOwner) {
            if (it.isConnected()) {
                viewModel.hideNoNetworkState()
            } else {
                viewModel.showNoNetworkState()
            }
            shouldRefresh = it.isConnected()
        }
    }

    open fun refreshScreen() {}

    @CallSuper
    override fun showMessage(message: Message, anchorView: View?) {
        try {
            when (message.message) {
                is StringSource.Text -> {
                    logDebug("showBannerMessage: ${message.message.text}", TAG)
                }

                is StringSource.Res -> {
                    context?.let {
                        logDebug("showBannerMessage: ${message.message.getString(it)}", TAG)
                    }
                }
            }
            (activity as? MessageBannerHolder)?.showMessage(message, anchorView)
        } catch (e: Exception) {
            logError("showBannerMessage Exception: ${e.message}", e, TAG)
        }
    }


    override fun showBeta() {
        (activity as? MessageBannerHolder)?.showBeta()
    }

    // hierarchy for view -> content, empty state, error state, loader

    fun showLoader(message: String? = null) {
        try {
            val loaderView = view?.findViewById<FrameLayout>(R.id.loaderView)
            if (loaderView?.visibility != View.VISIBLE) {
                loaderView?.visibility = View.VISIBLE
            }
            if (!message.isNullOrEmpty() && loaderView?.visibility == View.VISIBLE) {
                logDebug("showLoader message: $message", TAG)
                val tvMessage = loaderView.findViewById<AppCompatTextView>(R.id.tvMessage)
                tvMessage?.text = message
            }
        } catch (e: Exception) {
            logError("showLoader Exception: ${e.message}", e, TAG)
        }
    }

    private fun hideLoader() {
        try {
            view?.findViewById<ComplexGestureRefreshView>(R.id.refreshLayout)?.isRefreshing = false
            val loaderView = view?.findViewById<FrameLayout>(R.id.loaderView)
            if (loaderView?.visibility != View.GONE) {
                loaderView?.visibility = View.GONE
            }
        } catch (e: Exception) {
            logError("hideLoader Exception: ${e.message}", e, TAG)
        }
    }

    protected fun showEmptyState() {
        logDebug("showEmptyState", TAG)
        try {
            val emptyStateView = view?.findViewById<FrameLayout>(R.id.emptyStateView)
            if (emptyStateView?.visibility != View.VISIBLE) {
                emptyStateView?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            logError("showEmptyState Exception: ${e.message}", e, TAG)
        }
    }

    protected fun hideEmptyState() {
        logDebug("hideEmptyState", TAG)
        try {
            val emptyStateView = view?.findViewById<FrameLayout>(R.id.emptyStateView)
            if (emptyStateView?.visibility != View.GONE) {
                emptyStateView?.visibility = View.GONE
            }
        } catch (e: Exception) {
            logError("hideEmptyState Exception: ${e.message}", e, TAG)
        }
    }

    private fun showNoInternetConnectionState() {
        logDebug("showNoInternetConnectionState", TAG)
        try {
            val noInternetConnectionStateView = view?.findViewById<FrameLayout>(R.id.noInternetConnectionView)
            if (noInternetConnectionStateView?.visibility != View.VISIBLE) {
                noInternetConnectionStateView?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            logError("showNoInternetConnectionState Exception: ${e.message}", e, TAG)
        }
    }

    private fun hideNoInternetConnectionState() {
        logDebug("hideNoInternetConnectionState", TAG)
        try {
            val noInternetConnectionStateView = view?.findViewById<FrameLayout>(R.id.noInternetConnectionView)
            if (noInternetConnectionStateView?.visibility != View.GONE) {
                noInternetConnectionStateView?.visibility = View.GONE
            }
        } catch (e: Exception) {
            logError("hideNoInternetConnectionState Exception: ${e.message}", e, TAG)
        }
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
        try {
            logDebug("showErrorState description: $description", TAG)
            val errorView = view?.findViewById<View>(R.id.errorView)
            val btnErrorActionOne = errorView?.findViewById<AppCompatButton>(R.id.btnErrorActionOne)
            val btnErrorActionTwo = errorView?.findViewById<AppCompatButton>(R.id.btnErrorActionTwo)
            val tvDescription =
                errorView?.findViewById<AppCompatTextView>(R.id.tvErrorViewDescription)
            val tvTitle =
                errorView?.findViewById<AppCompatTextView>(R.id.tvErrorViewTitle)
            val ivIcon =
                errorView?.findViewById<AppCompatImageView>(R.id.ivErrorIcon)
            errorView?.visibility = View.VISIBLE
            if (showTitle != null) {
                tvTitle?.isVisible = showTitle
            }
            if (showDescription != null) {
                tvDescription?.isVisible = showDescription
            }
            if (showActionOneButton != null) {
                btnErrorActionTwo?.isVisible = showActionOneButton
            }
            if (showActionTwoButton != null) {
                btnErrorActionTwo?.isVisible = showActionTwoButton
            }
            if (showIcon != null) {
                ivIcon?.isVisible = showIcon
            }
            if (title != null) {
                tvTitle?.text = title.getString(requireContext())
            }
            if (description != null) {
                tvDescription?.text = description.getString(requireContext())
            }
            if (actionOneButtonText != null) {
                btnErrorActionOne?.text = actionOneButtonText.getString(requireContext())
            }
            if (actionTwoButtonText != null) {
                btnErrorActionTwo?.text = actionTwoButtonText.getString(requireContext())
            }
            if (iconRes != null && iconRes != 0) {
                ivIcon?.setImageResource(iconRes)
            }
        } catch (e: Exception) {
            logError("showErrorState Exception: ${e.message}", e, TAG)
        }
    }

    private fun hideErrorState() {
        try {
            val errorView = view?.findViewById<FrameLayout>(R.id.errorView)
            errorView?.visibility = View.GONE
        } catch (e: Exception) {
            logError("hideErrorState Exception: ${e.message}", e, TAG)
        }
    }

    override fun onBackPressed() {
        // Default on back pressed implementation for fragments.
        viewModel.onBackPressed()
    }

    @CallSuper
    override fun onDestroyView() {
        if (!shouldKeepBinding) {
            viewBinding = null
        }
        viewModel.unbindFlowNavController()
        viewModel.unbindActivityNavController()
        super.onDestroyView()
    }

    protected fun showSettingsMenu() {
        logDebug("showSettingsMenu", TAG)
        val menuBinding = LayoutAppMenuBinding.inflate(layoutInflater).apply {
            icClose.onClickThrottle { hideSettingsMenu() }
            tvSettings.onClickThrottle { viewModel.onSettingsClicked() }
            tvQuestions.onClickThrottle { viewModel.onFaqClicked() }
            tvContacts.onClickThrottle { viewModel.onContactsClicked() }
            tvConditions.onClickThrottle { viewModel.onConditionsClicked() }
        }
        popupWindow = PopupWindow(
            menuBinding.root,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow?.contentView?.let {
            it.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        popupWindow?.showAtLocation(menuBinding.root, Gravity.TOP, 320, 0)
    }

    private fun hideSettingsMenu() {
        logDebug("hideSettingsMenu", TAG)
        popupWindow?.setOnDismissListener {
            popupWindow = null
        }
        popupWindow?.dismiss()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        viewModel.fragmentOnResume()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        hideSettingsMenu()
        viewModel.fragmentOnPause()
    }

}
