package com.digitall.digital_sofia.ui.fragments.base

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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.LayoutAppMenuBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.findActivityNavController
import com.digitall.digital_sofia.extensions.onClickThrottle
import com.digitall.digital_sofia.models.common.BackFragment
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.ErrorState
import com.digitall.digital_sofia.models.common.LoadingState
import com.digitall.digital_sofia.models.common.MessageBannerHolder
import com.digitall.digital_sofia.models.common.StringSource
import com.digitall.digital_sofia.models.common.UiState
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.ui.view.ComplexGestureRefreshView

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment(),
    BackFragment,
    MessageBannerHolder {

    companion object {
        private const val TAG = "BaseFragmentTag"
    }

    abstract val viewModel: VM

    private var viewBinding: VB? = null

    private var popupWindow: PopupWindow? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    val binding get() = viewBinding!!

    abstract fun getViewBinding(): VB

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
        subscribeToBaseViewModel()
        super.onViewCreated(view, savedInstanceState)
        viewModel.attachView()
        onCreated()
        setupView()
        setupControls()
        subscribeToLiveData()
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

    private fun subscribeToBaseViewModel() {
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
        viewModel.showBannerMessageLiveData.observe(viewLifecycleOwner) { message ->
            showBannerMessage(message)
        }
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Ready -> hideEmptyState()
                is UiState.Empty -> showEmptyState()
            }
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                is LoadingState.Ready -> hideLoader()
                is LoadingState.Loading -> showLoader()
            }
        }
        viewModel.errorState.observe(viewLifecycleOwner) {
            when (it) {
                is ErrorState.Ready -> hideErrorState()
                is ErrorState.Error -> showErrorState(
                    title = it.title,
                    iconRes = it.iconRes,
                    showIcon = it.showIcon,
                    showTitle = it.showTitle,
                    description = it.description,
                    showDescription = it.showDescription,
                    showReloadButton = it.showReloadButton,
                    reloadButtonText = it.reloadButtonText,
                )
            }
        }
    }

    @CallSuper
    override fun showBannerMessage(message: BannerMessage, anchorView: View?) {
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
            (activity as? MessageBannerHolder)?.showBannerMessage(message, anchorView)
        } catch (e: Exception) {
            logError("showBannerMessage Exception: ${e.message}", e, TAG)
        }
    }

    // hierarchy for view -> content, empty state, error state, loader

    fun showLoader() {
        try {
            val loaderView = view?.findViewById<FrameLayout>(R.id.loaderView)
            if (loaderView?.visibility != View.VISIBLE) {
                loaderView?.visibility = View.VISIBLE
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

    private fun showEmptyState() {
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

    private fun hideEmptyState() {
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

    private fun showErrorState(
        iconRes: Int?,
        showIcon: Boolean?,
        showTitle: Boolean?,
        title: StringSource?,
        showDescription: Boolean?,
        description: StringSource?,
        showReloadButton: Boolean?,
        reloadButtonText: StringSource?,
    ) {
        logDebug("showErrorState", TAG)
        try {
            logDebug("showErrorState description: $description", TAG)
            val errorView = view?.findViewById<View>(R.id.errorView)
            val btnReload = errorView?.findViewById<AppCompatButton>(R.id.btnErrorViewReload)
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
            if (showReloadButton != null) {
                btnReload?.isVisible = showReloadButton
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
            if (reloadButtonText != null) {
                btnReload?.text = reloadButtonText.getString(requireContext())
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
        viewBinding = null
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
        viewModel.startUpdateDocumentsIfNeed()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        hideSettingsMenu()
        viewModel.stopUpdateDocuments()
    }

}
