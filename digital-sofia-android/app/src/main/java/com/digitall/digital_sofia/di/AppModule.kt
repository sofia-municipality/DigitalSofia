package com.digitall.digital_sofia.di

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.digitall.digital_sofia.data.di.IO_DISPATCHER
import com.digitall.digital_sofia.data.di.MAIN_DISPATCHER
import com.digitall.digital_sofia.data.utils.CoroutineContextProvider
import com.digitall.digital_sofia.domain.repository.common.PreferencesRepository
import com.digitall.digital_sofia.domain.usecase.documents.DocumentsUseCase
import com.digitall.digital_sofia.mappers.common.PermissionNamePmMapper
import com.digitall.digital_sofia.utils.ActivitiesCommonHelper
import com.digitall.digital_sofia.utils.CurrentContext
import com.digitall.digital_sofia.utils.DownloadHelper
import com.digitall.digital_sofia.utils.EvrotrustSDKHelper
import com.digitall.digital_sofia.utils.EvrotrustSDKHelperImpl
import com.digitall.digital_sofia.utils.LocalizationManager
import com.digitall.digital_sofia.utils.NotificationHelper
import com.digitall.digital_sofia.utils.PermissionsManager
import com.digitall.digital_sofia.utils.PermissionsManagerImpl
import com.digitall.digital_sofia.utils.ScreenshotsDetector
import com.digitall.digital_sofia.utils.ScreenshotsDetectorImpl
import com.digitall.digital_sofia.utils.SocialNetworksHelper
import com.digitall.digital_sofia.utils.SupportBiometricManager
import com.digitall.digital_sofia.utils.SupportBiometricManagerImpl
import com.digitall.digital_sofia.utils.UpdateDocumentsHelper
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

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
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) {
        Dispatchers.Main
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
    single<RootBeer> {
        RootBeer(androidContext())
    }
    single<ScreenshotsDetector> {
        ScreenshotsDetectorImpl(
            context = get<CurrentContext>(),
            handler = get<Handler>(),
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
            context = get<CurrentContext>(),
        )
    }
    single<PermissionsManager> {
        PermissionsManagerImpl(
            permissionNamePmMapper = get<PermissionNamePmMapper>(),
            context = get<CurrentContext>(),
        )
    }
    single<SupportBiometricManager> {
        SupportBiometricManagerImpl()
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
    single<LocalizationManager> {
        LocalizationManager(
            preferences = get<PreferencesRepository>(),
            currentContext = get<CurrentContext>(),
        )
    }
    single<UpdateDocumentsHelper> {
        UpdateDocumentsHelper(
            documentsUseCase = get<DocumentsUseCase>(),
            notificationHelper = get<NotificationHelper>(),
        )
    }
}