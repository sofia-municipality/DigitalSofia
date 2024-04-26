/**
 * Use single activity
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.ui.activity

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.digital.sofia.BuildConfig
import com.digital.sofia.R
import com.digital.sofia.databinding.ActivityBaseBinding
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.backgroundColor
import com.digital.sofia.extensions.hideKeyboard
import com.digital.sofia.extensions.throwBackPressedEvent
import com.digital.sofia.extensions.wrap
import com.digital.sofia.models.common.AlertDialogResult
import com.digital.sofia.models.common.Message
import com.digital.sofia.models.common.MessageBannerHolder
import com.digital.sofia.models.common.StartDestination
import com.digital.sofia.ui.view.BetaView
import com.digital.sofia.utils.AlertDialogResultListener
import com.digital.sofia.utils.AppUncaughtExceptionHandler
import com.digital.sofia.utils.BannerMessageWindowManager
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.PersistentFragmentFactory
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.UpdateDocumentsHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(),
    MessageBannerHolder,
    ComponentCallbacks2 {

    companion object {
        private const val TAG = "MainActivityTag"
    }

    private fun getStartDestination(): StartDestination {
        return viewModel.getStartDestination(intent)
    }

    lateinit var binding: ActivityBaseBinding

    private lateinit var bannerMessageWindowManager: BannerMessageWindowManager

    private val viewModel: MainViewModel by viewModel()
    private val loginTimer: LoginTimer by inject()
    private val appContext: Context by inject()
    private val currentContext: CurrentContext by inject()
    private val evrotrustSDKHelper: EvrotrustSDKHelper by inject()
    private val biometricManager: SupportBiometricManager by inject()
    private val updateDocumentsHelper: UpdateDocumentsHelper by inject()
    private val persistentFragmentFactory: PersistentFragmentFactory by inject()
    private val preferences: PreferencesRepository by inject()

    var alertDialogResultListener: AlertDialogResultListener? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            viewModel.dispatchTouchEvent()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logDebug("onCreate", TAG)
        installSplashScreen()
        supportFragmentManager.fragmentFactory = persistentFragmentFactory
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT <= 29 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }
        val isBiometricAvailable = biometricManager.hasBiometrics()
        if (isBiometricAvailable) {
            biometricManager.setupBiometricManager(this)
        }
        viewModel.onNewIntent(intent)
//        if (!notificationManager.areNotificationsEnabled()) {
//            val intent = Intent().apply {
//                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
//                putExtra(Settings.EXTRA_APP_PACKAGE, this@BaseActivity.packageName)
//            }
//            startActivity(intent)
//        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val language = preferences.readCurrentLanguage()
        val newContext = ContextWrapper(newBase).wrap(language.type)
        super.attachBaseContext(newContext)
    }

    private fun hideSystemUI() {
        window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
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
        viewModel.showMessageLiveData.observe(this) {
            showMessage(it)
        }
        updateDocumentsHelper.showBannerMessageLiveData.observe(this) {
            showMessage(it)
        }
        loginTimer.lockStatusLiveData.observe(this) {
            if (it) {
                logDebug("lockStatusLiveData onLoginTimerExpired", TAG)
                viewModel.onLoginTimerExpired()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logDebug("onSaveInstanceState", TAG)
        viewModel.onSaveInstanceState(outState)
    }

    override fun showMessage(message: Message, anchorView: View?) {
        logDebug("showMessage message: ${message.message.getString(this)}", TAG)
        try {
            when (message.type) {
                Message.Type.MESSAGE -> {
                    bannerMessageWindowManager.showMessage(
                        message = message,
                        anchorView = anchorView ?: binding.rootLayout,
                    )
                }

                Message.Type.ALERT -> {
                    val builder = AlertDialog.Builder(this)
                        .setMessage(message.message.getString(this))
                    if (message.title != null) {
                        builder.setTitle(message.title.getString(this))
                    }
                    if (message.positiveButtonText != null) {
                        builder.setPositiveButton(message.positiveButtonText.getString(this)) { dialog, _ ->
                            logDebug("alertDialog result positive", TAG)
                            dialog.cancel()
                            alertDialogResultListener?.onAlertDialogResult(
                                AlertDialogResult(
                                    messageId = message.messageId,
                                    isPositive = true,
                                )
                            )
                        }
                    }
                    if (message.negativeButtonText != null) {
                        builder.setNegativeButton(message.negativeButtonText.getString(this)) { dialog, _ ->
                            logDebug("alertDialog result negative", TAG)
                            dialog.cancel()
                            alertDialogResultListener?.onAlertDialogResult(
                                AlertDialogResult(
                                    messageId = message.messageId,
                                    isPositive = false,
                                )
                            )
                        }
                    }
                    builder.setOnCancelListener { _: DialogInterface? ->
                        logDebug("alertDialog result negative", TAG)
                        alertDialogResultListener?.onAlertDialogResult(
                            AlertDialogResult(
                                messageId = message.messageId,
                                isPositive = false,
                            )
                        )
                    }
                    builder.create()
                        .show()
                }
            }
        } catch (e: Exception) {
            logError("showBannerMessage Exception: ${e.message}", e, TAG)
        }
    }

    override fun showBeta() {
        val builder = AlertDialog.Builder(this, R.style.BetaAlertDialog)
        val betaView = BetaView(context = this)
        builder.setView(betaView)

        builder.create()
            .show()
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

    override fun onResume() {
        logDebug("onResume", TAG)
        super.onResume()
        viewModel.onResume(this)
    }

    override fun onPause() {
        logDebug("onPause", TAG)
        hideKeyboard()
        viewModel.onPause(this)
        super.onPause()
    }

    private fun onBackPressedInternal() {
        logDebug("onBackPressedInternal", TAG)
        val handled = supportFragmentManager.throwBackPressedEvent(R.id.navigationContainer)
        if (!handled) finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        logDebug("onDestroy", TAG)
        viewModel.onDestroy()
        bannerMessageWindowManager.hideWindow()
        // Reset this activity context
        if (currentContext.get() == this) {
            currentContext.attachBaseContext(appContext)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logDebug("onActivityResult requestCode: $requestCode resultCode: $resultCode", TAG)
        evrotrustSDKHelper.onActivityResult(
            requestCode = requestCode,
            data = data,
        )
    }

}