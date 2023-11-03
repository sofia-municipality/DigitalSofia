package com.digitall.digital_sofia.ui.activity.base

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.digitall.digital_sofia.BuildConfig
import com.digitall.digital_sofia.R
import com.digitall.digital_sofia.databinding.ActivityBaseBinding
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import com.digitall.digital_sofia.extensions.hideKeyboard
import com.digitall.digital_sofia.extensions.throwBackPressedEvent
import com.digitall.digital_sofia.models.common.BannerMessage
import com.digitall.digital_sofia.models.common.MessageBannerHolder
import com.digitall.digital_sofia.models.common.StartDestination
import com.digitall.digital_sofia.utils.BannerMessageWindowManager
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import org.koin.android.ext.android.inject

/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */

abstract class BaseActivity<VM : BaseActivityViewModel> : AppCompatActivity(),
    MessageBannerHolder,
    ComponentCallbacks2 {

    companion object {
        private const val TAG = "BaseActivityTag"
    }

    abstract val viewModel: VM

    protected open fun onCreated() {
        // Override when needed
    }

    private fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination(intent)
    }

    lateinit var binding: ActivityBaseBinding

    private val appContext: Context by inject()

    private val currentContext: CurrentContext by inject()

    private val updateDocumentsHelper: UpdateDocumentsHelper by inject()

    private lateinit var bannerMessageWindowManager: BannerMessageWindowManager

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(AppUncaughtExceptionHandler())
        viewModel.applyLightDarkTheme()
        currentContext.attachBaseContext(this)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bannerMessageWindowManager = BannerMessageWindowManager(this)
        setupOnBackPressedCallback()
        setupNavController()
        subscribeToLiveData()
        viewModel.attachView()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()
        savedInstanceState?.let(viewModel::onRestoreInstanceState)
        onCreated()
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT <= 29 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }
    }

    private fun hideSystemUI() {
        window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this) {
            onBackPressedInternal()
        }
    }

    private fun setupNavController() {
        val host =
            supportFragmentManager.findFragmentById(R.id.navigationContainer) as NavHostFragment
        try {
            // Try to get the current graph, if it is there, nav controller is valid.
            // When there is no graph, it throws IllegalStateException,
            // then we need to create a graph ourselves
            host.navController.graph
        } catch (e: Exception) {
            val graphInflater = host.navController.navInflater
            val graph = graphInflater.inflate(R.navigation.nav_activity)
            val startDestination = getStartDestination()
            graph.setStartDestination(startDestination.destination)
            host.navController.setGraph(graph, startDestination.arguments)
        }
        viewModel.bindActivityNavController(host.navController)
    }

    private fun subscribeToLiveData() {
        viewModel.closeActivityLiveData.observe(this) {
            finish()
        }
        viewModel.showBannerMessageLiveData.observe(this) {
            showBannerMessage(it)
        }
        updateDocumentsHelper.showBannerMessageLiveData.observe(this) {
            showBannerMessage(it)
        }
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.onSaveInstanceState(outState)
    }

    final override fun showBannerMessage(message: BannerMessage, anchorView: View?) {
        try {
            bannerMessageWindowManager.showMessage(message, anchorView ?: binding.rootLayout)
        } catch (e: Exception) {
            logError("showBannerMessage Exception: ${e.message}", e, TAG)
        }
    }

    fun onPasswordRequiredAuthError() {
        viewModel.onPasswordRequiredAuthError()
    }

    fun onProfileBlockedError() {
        viewModel.onProfileBlockedError()
    }

    fun onForceUpdateError() {
        viewModel.onForceUpdateError()
    }

//    @CallSuper
//    override fun onResume() {
//        super.onResume()
//        viewModel.onResume(this)
//    }

    @CallSuper
    override fun onPause() {
        hideKeyboard()
        super.onPause()
        viewModel.onPause(this)
    }

    private fun onBackPressedInternal() {
        val handled = supportFragmentManager.throwBackPressedEvent(R.id.navigationContainer)
        if (!handled) finish()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        bannerMessageWindowManager.hideWindow()
        // Reset this activity context
        if (currentContext.get() == this) {
            currentContext.attachBaseContext(appContext)
        }
    }
}