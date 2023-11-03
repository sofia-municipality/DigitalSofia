package com.digitall.digital_sofia.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.digitall.digital_sofia.data.database.converters.BigDecimalToStringConverter
import com.digitall.digital_sofia.data.database.converters.DateToLongConverter
import com.digitall.digital_sofia.data.database.converters.StringListConverter
import com.digitall.digital_sofia.data.database.dao.documents.DocumentsDao
import com.digitall.digital_sofia.data.models.database.documents.DocumentsEntity

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

@Database(
    entities = [
        DocumentsEntity::class,
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    value = [
        BigDecimalToStringConverter::class,
        DateToLongConverter::class,
        StringListConverter::class,
    ]
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun getDocumentsDao(): DocumentsDao

}