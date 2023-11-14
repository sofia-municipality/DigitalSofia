package com.digitall.digital_sofia.ui.fragments.base

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.core.animation.addListener
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.BottomSheetBaseLayoutBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logDebug
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.pxDimen
import com.digitall.digital_sofia.models.common.BackFragment
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.MessageBannerHolder
import com.digitall.digital_sofia.ui.BaseViewModel
import com.digitall.digital_sofia.utils.SoftKeyboardStateWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * This base class already has a State Holder Layout inside, but it is on
 * the full size of the modal, so if the embedded state holder is needed,
 * the other layout should be used and the base methods like,
 * [showLoader], [showReady], [showNetworkError] etc. should be overridden.
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseBottomSheetFragment<VB : ViewBinding, VM : BaseViewModel> :
    BottomSheetDialogFragment(),
    BackFragment,
    MessageBannerHolder {

    companion object {
        private const val TAG = "BaseBottomSheetFragmentTag"
        private const val ANIMATION_DURATION = 250L
    }

    protected open val isDraggableByUser: Boolean = true
    protected open val isCancelableFromOutside: Boolean = true

    private var isPostponedBottomSheet: Boolean = false

    abstract val viewModel: VM

    private var viewBinding: VB? = null

    private var rootViewBinding: BottomSheetBaseLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    val binding get() = viewBinding!!

    abstract fun getViewBinding(): VB

    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    private var sheetFrame: FrameLayout? = null
    private var mode = BottomDialogMode.DEFAULT
    private var isAnimated = false
    private var currentAnimation: ValueAnimator? = null
    private var sheetHeight = 0
    private val layoutChangeListener = ViewTreeObserver.OnPreDrawListener {
        onLayoutMeasuresChanged()
    }

    private var mSoftKeyboardStateWatcher: SoftKeyboardStateWatcher? = null
    private var mSoftKeyboardStateListener =
        object : SoftKeyboardStateWatcher.SoftKeyboardStateListener {
            override fun onSoftKeyboardOpened(keyboardHeight: Int) {
                onKeyBoardOpened(keyboardHeight)
            }

            override fun onSoftKeyboardClosed() {
                onKeyboardClosed()
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logDebug("onCreateDialog", TAG)
        return CustomDialog(requireContext(), R.style.AppBottomSheetDialogTheme).apply {
            setCanceledOnTouchOutside(isCancelableFromOutside)
            setOnShowListener {
                // Find a Frame layout with Bottom sheet behavior in the dialog
                (it as? BottomSheetDialog)
                    ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.let { sheet ->
                        setupBottomSheet(sheet)
                    }

                if (mode != BottomDialogMode.DEFAULT) {
                    setMode(mode)
                }

                onBottomSheetDialogShown()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logDebug("onCreateView", TAG)
        rootViewBinding = BottomSheetBaseLayoutBinding.inflate(layoutInflater)
        viewBinding = getViewBinding()
        rootViewBinding?.flBottomSheetContainer?.addView(viewBinding!!.root)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        if (!showsDialog) {
            setMode(mode)
        }
        return rootViewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logDebug("onViewCreated", TAG)
        (view.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        setupNavControllers()
        subscribeToLiveData()
        initViews()
        super.onViewCreated(view, savedInstanceState)
        viewModel.attachView()
//        viewModel.trackScreenOpenningAnalyticEvent(
//            this::class.java.simpleName, tryToFindScreenTitle()
//        )
        // Setup Watchers
        mSoftKeyboardStateWatcher = SoftKeyboardStateWatcher(requireActivity())
        mSoftKeyboardStateWatcher?.setStatusBarOffset(getStatusBarHeight())
    }

    protected open fun setupNavControllers() {
        setupActivityNavController()
    }

    private fun setupActivityNavController() {
        // Search for the activity controller
        val host = requireActivity().supportFragmentManager
            .findFragmentById(R.id.navigationContainer) as NavHostFragment
        viewModel.bindActivityNavController(host.navController)
    }

    protected open fun initViews() {
        // Override when needed
    }

    protected open fun onBottomSheetDialogShown() {
        // Override when needed
    }

    private fun subscribeToLiveData() {
        viewModel.closeActivityLiveData.observe(viewLifecycleOwner) {
            requireActivity().finish()
        }
        viewModel.backPressedFailedLiveData.observe(viewLifecycleOwner) {
            dismiss()
        }
        viewModel.showBannerMessageLiveData.observe(viewLifecycleOwner) { message ->
            showBannerMessage(message)
        }
    }

    override fun onBackPressed() {
        logDebug("onBackPressed", TAG)
        dismiss()
    }

    open fun onKeyBoardOpened(keyboardHeight: Int) {
        logDebug("onKeyBoardOpened", TAG)
        rootViewBinding?.apply {
            rootLayout.post {
                rootLayout.setPadding(0, 0, 0, keyboardHeight)
            }
        }
    }

    open fun onKeyboardClosed() {
        logDebug("onKeyboardClosed", TAG)
        rootViewBinding?.apply {
            rootLayout.post {
                rootLayout.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun showBannerMessage(message: BannerMessage, anchorView: View?) {
        try {
            (requireActivity() as MessageBannerHolder)
                .showBannerMessage(message, anchorView ?: binding.root)
        } catch (e: Exception) {
            logError("showBannerMessage Exception: ${e.message}", e, TAG)
        }
    }

    fun logoutFromApplication() {
        // TODO
    }

    override fun onResume() {
        super.onResume()
        mSoftKeyboardStateWatcher?.addSoftKeyboardStateListener(mSoftKeyboardStateListener)
    }

    override fun onPause() {
        super.onPause()
        mSoftKeyboardStateWatcher?.removeSoftKeyboardStateListener(mSoftKeyboardStateListener)
        onKeyboardClosed()
    }

    /**
     * Setup bottom sheet behaviour and save a link to the sheet layout
     *
     * @param sheet - bottom sheet frame layout
     */
    private fun setupBottomSheet(sheet: FrameLayout) {
        sheetFrame = sheet
        behavior = BottomSheetBehavior.from(sheetFrame!!)
        behavior?.apply {
            peekHeight = 0
            isHideable = true
            skipCollapsed = true
            isDraggable = isDraggableByUser
            if (!isPostponedBottomSheet) {
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    fun postponeEnterBottomSheet() {
        this.isPostponedBottomSheet = true
    }

    fun startPostponedEnterBottomSheet() {
        if (isPostponedBottomSheet) {
            isPostponedBottomSheet = false
            sheetFrame?.post { behavior?.state = BottomSheetBehavior.STATE_EXPANDED }
        }
    }

    /**
     * Change current dialog mode. Its changed the height calculation
     * mechanic.
     *
     * @param mode - a new mode of the dialog
     * @see BottomDialogMode
     */
    fun setMode(mode: BottomDialogMode) {
        this.mode = mode
        when (mode) {
            BottomDialogMode.DEFAULT -> {
                subscribeToLayoutChanges()
            }

            BottomDialogMode.FULL_SCREEN -> {
                releaseLayoutChangesListener()
                sheetFrame?.let {
                    it.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                    it.requestLayout()
                    it.viewTreeObserver.addOnPreDrawListener(layoutChangeListener)
                }
            }
        }
    }

    private fun subscribeToLayoutChanges() {
        sheetFrame?.let {
            it.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
            it.requestLayout()
            it.viewTreeObserver.addOnPreDrawListener(layoutChangeListener)
        }
    }

    private fun releaseLayoutChangesListener() {
        sheetFrame?.viewTreeObserver?.removeOnPreDrawListener(layoutChangeListener)
    }

    /**
     * Listen the layout changes in pre draw method and handle it
     *
     * @return true when we have to cancel a system draw event and
     *         recalculate own height, false otherwise
     */
    private fun onLayoutMeasuresChanged(): Boolean {
        val newHeight = sheetFrame?.measuredHeight ?: 0
        // Check that height changed
        if (sheetHeight != newHeight && newHeight != 0) {
            if (isAnimated) {
                currentAnimation?.cancel()
            }

            animateContainerHeight(sheetHeight, newHeight)
            return false
        }
        return true
    }

    /**
     * Resize bottom dialog container to appropriate view height.
     * Resizing include a simple animation transition
     *
     * @param oldHeight - the old height of the container
     * @param height - a new view height, that of course also a new container height
     */
    private fun animateContainerHeight(oldHeight: Int, height: Int) {
        sheetFrame?.let { frame ->
            currentAnimation = ValueAnimator.ofInt(oldHeight, height)?.apply {
                addUpdateListener { valueAnimator ->
                    sheetHeight = valueAnimator.animatedValue as Int
                    val layoutParams = frame.layoutParams
                    layoutParams.height = sheetHeight
                    frame.layoutParams = layoutParams
                }
                addListener(
                    { onEndAnimationListener() },
                    { onStartAnimationListener() },
                    { onCancelAnimationListener() }
                )
                duration = ANIMATION_DURATION
                start()
            }
        }
    }

    private fun onStartAnimationListener() {
        releaseLayoutChangesListener()
        isAnimated = true
    }

    private fun onEndAnimationListener() {
        isAnimated = false
        if (mode == BottomDialogMode.DEFAULT) {
            subscribeToLayoutChanges()
        }
    }

    private fun onCancelAnimationListener() {
        isAnimated = false
        if (mode == BottomDialogMode.DEFAULT) {
            subscribeToLayoutChanges()
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.pxDimen(resourceId)
        } else 0
    }

    override fun onDismiss(dialog: DialogInterface) {
        // Prevent memory leak
        (dialog as? BottomSheetDialog)?.setOnShowListener(null)
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        viewBinding = null
        viewModel.unbindActivityNavController()
        dialog?.setOnShowListener(null)
        releaseLayoutChangesListener()
        super.onDestroyView()
    }

    // Handles back press
    inner class CustomDialog(context: Context, theme: Int) : BottomSheetDialog(context, theme) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setupOnBackPressedCallback()
            if (isPostponedBottomSheet) {
                // to prevent auto displaying bottom sheet content when created
                // in postponed mode only
                behavior.peekHeight = 0
            }
        }

        private fun setupOnBackPressedCallback() {
            onBackPressedDispatcher.addCallback(this) {
                this@BaseBottomSheetFragment.onBackPressed()
            }
        }

        override fun onStart() {
            super.onStart()
            if (!isPostponedBottomSheet) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    /**
     * This is the modes for a dialog.
     * FULL_SCREEN - set to the dialog a MATCH_PARENT size and fix it.
     * DEFAULT - set to the dialog a WRAP_CONTENT size and allow to change it
     *           according to content with the animated transitions.
     */
    enum class BottomDialogMode {
        FULL_SCREEN,
        DEFAULT
    }

}