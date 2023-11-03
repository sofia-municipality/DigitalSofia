package com.digitall.digital_sofia.data.di

import androidx.room.Room
import com.digitall.digital_sofia.data.DATABASE_NAME
import com.digitall.digital_sofia.data.PROPERTY_USER_PASSWORD
import com.digitall.digital_sofia.data.database.AppDatabase
import com.digitall.digital_sofia.data.database.dao.documents.DocumentsDao
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
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

val databaseModule = module {
    single<AppDatabase> {
        try {
            val passPhrase = getKoin().getProperty(PROPERTY_USER_PASSWORD, "")
            val passPhraseBytes: ByteArray = SQLiteDatabase.getBytes(passPhrase.toCharArray())
            val factory = SupportFactory(passPhraseBytes)
            Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build().also {
                    // Check if DB create correctly. If no - will be exception.
                    it.openHelper.readableDatabase.isDatabaseIntegrityOk
                }
        } catch (e: Exception) {
            Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    factory<DocumentsDao> {
        get<AppDatabase>().getDocumentsDao()
    }
}