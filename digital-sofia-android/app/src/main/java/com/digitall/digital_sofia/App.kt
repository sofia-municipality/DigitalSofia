package com.digitall.digital_sofia

import android.app.Application
import com.digitall.digital_sofia.data.di.dataModules
import com.digitall.digital_sofia.di.appModules
import com.digitall.digital_sofia.domain.di.domainModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class App : Application() {

    companion object {
        private const val TAG = "AppTag"
        lateinit var app: App
        fun getInstance() = app
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        setupKoin()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            if (BuildConfig.DEBUG) {
                androidLogger(Level.ERROR)
            }
            modules(appModules + domainModules + dataModules)
        }
    }


}

