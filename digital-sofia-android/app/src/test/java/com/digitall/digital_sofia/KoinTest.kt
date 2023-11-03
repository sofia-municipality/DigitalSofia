package com.digitall.digital_sofia

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.digitall.digital_sofia.di.appModules
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.verify.verifyAll
import org.mockito.Mockito

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class KoinTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun `test DI modules`() {
        appModules.verifyAll()
//        koinApplication {
//            modules(appModules + domainModules + dataModules)
//            checkModules {
//                withInstance<Context>()
//                withInstance<Application>()
//                withInstance<SavedStateHandle>()
//                withInstance<WorkerParameters>()
//            }
//        }
    }

}