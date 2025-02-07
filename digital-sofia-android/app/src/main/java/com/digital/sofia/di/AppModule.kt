/**
 * single<Class>(named("name")){Class()} -> for creating a specific instance in module
 * single<Class1>{Class1(get<Class2>(named("name")))} -> for creating a specific instance in module
 * val nameOfVariable: Class by inject(named("name")) -> for creating a specific instance in class
 * get<Class>{parametersOf("param")} -> parameter passing in module
 * single<Class>{(param: String)->Class(param)} -> parameter passing in module
 * val name: Class by inject(parameters={parametersOf("param")}) -> parameter passing in class
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 */
package com.digital.sofia.di

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.digital.sofia.data.utils.CoroutineContextProvider
import com.digital.sofia.domain.repository.common.PreferencesRepository
import com.digital.sofia.mappers.common.PermissionNamePmMapper
import com.digital.sofia.utils.ActivitiesCommonHelper
import com.digital.sofia.utils.AppEventsHelper
import com.digital.sofia.utils.CurrentContext
import com.digital.sofia.utils.DownloadHelper
import com.digital.sofia.utils.EvrotrustSDKHelper
import com.digital.sofia.utils.EvrotrustSDKHelperImpl
import com.digital.sofia.utils.FirebaseMessagingServiceHelper
import com.digital.sofia.utils.LocalizationManager
import com.digital.sofia.utils.LoginTimer
import com.digital.sofia.utils.LoginTimerImpl
import com.digital.sofia.utils.MainThreadExecutor
import com.digital.sofia.utils.NetworkConnectionManager
import com.digital.sofia.utils.NetworkConnectionManagerImpl
import com.digital.sofia.utils.NotificationHelper
import com.digital.sofia.utils.PermissionsManager
import com.digital.sofia.utils.PermissionsManagerImpl
import com.digital.sofia.utils.PersistentFragmentFactory
import com.digital.sofia.utils.RecyclerViewAdapterDataObserver
import com.digital.sofia.utils.ScreenshotsDetector
import com.digital.sofia.utils.ScreenshotsDetectorImpl
import com.digital.sofia.utils.SocialNetworksHelper
import com.digital.sofia.utils.SupportBiometricManager
import com.digital.sofia.utils.SupportBiometricManagerImpl
import com.scottyab.rootbeer.RootBeer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single<ClipboardManager> {
        ContextCompat.getSystemService(
            androidContext(),
            ClipboardManager::class.java
        ) as ClipboardManager
    }

    single<DownloadManager> {
        ContextCompat.getSystemService(
            androidContext(),
            DownloadManager::class.java
        ) as DownloadManager
    }

    single<NotificationManager> {
        ContextCompat.getSystemService(
            androidContext(),
            NotificationManager::class.java
        ) as NotificationManager
    }

    single<CoroutineContextProvider> {
        CoroutineContextProvider()
    }

    single<ActivitiesCommonHelper> {
        ActivitiesCommonHelper(
            preferences = get<PreferencesRepository>(),
        )
    }

    single<NotificationHelper> {
        NotificationHelper(
            currentContext = get<CurrentContext>(),
            notificationManager = get<NotificationManager>(),
        )
    }

    single<DownloadHelper> {
        DownloadHelper(
            downloadManager = get<DownloadManager>(),
            preferences = get<PreferencesRepository>(),
        )
    }

    single<RecyclerViewAdapterDataObserver> {
        RecyclerViewAdapterDataObserver()
    }

    single<RootBeer> {
        RootBeer(androidContext())
    }

    single<ScreenshotsDetector> {
        ScreenshotsDetectorImpl(
            handler = get<Handler>(),
            currentContext = get<CurrentContext>(),
        )
    }

    single<Handler> {
        Handler(Looper.getMainLooper())
    }

    single<SocialNetworksHelper> {
        SocialNetworksHelper()
    }

    single<PermissionNamePmMapper> {
        PermissionNamePmMapper(
            currentContext = get<CurrentContext>(),
        )
    }

    single<PermissionsManager> {
        PermissionsManagerImpl(
            currentContext = get<CurrentContext>(),
            permissionNamePmMapper = get<PermissionNamePmMapper>(),
        )
    }

    single<MainThreadExecutor> {
        MainThreadExecutor()
    }

    single<SupportBiometricManager> {
        SupportBiometricManagerImpl(
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
            mainThreadExecutor = get<MainThreadExecutor>(),
        )
    }

    single<CurrentContext> {
        CurrentContext(
            context = androidContext()
        )
    }

    single<EvrotrustSDKHelper> {
        EvrotrustSDKHelperImpl(
            currentContext = get<CurrentContext>(),
            preferences = get<PreferencesRepository>(),
        )
    }

    single<LoginTimer> {
        LoginTimerImpl()
    }

    single<LocalizationManager> {
        LocalizationManager(
            preferences = get<PreferencesRepository>(),
            currentContext = get<CurrentContext>(),
        )
    }

    single<FirebaseMessagingServiceHelper> {
        FirebaseMessagingServiceHelper(
            preferences = get<PreferencesRepository>(),
        )
    }

    single<AppEventsHelper> {
        AppEventsHelper(
            notificationHelper = get<NotificationHelper>(),
        )
    }

    single {
        PersistentFragmentFactory()
    }

    single<NetworkConnectionManager> {
        NetworkConnectionManagerImpl(context = get<CurrentContext>())
    }

}