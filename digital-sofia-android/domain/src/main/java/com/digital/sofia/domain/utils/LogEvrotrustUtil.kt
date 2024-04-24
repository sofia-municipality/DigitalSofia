package com.digital.sofia.domain.utils

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import com.digital.sofia.domain.models.log.LogData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object LogEvrotrustUtil {

    private val timeDirectoryName: String

    private const val QUEUE_CAPACITY = 10000
    private const val CURRENT_TAG = "LogEvrotrustUtilExecutionStatusTag"
    private const val LOG_APP_FOLDER_NAME = "DigitalSofia"
    private const val TIME_FORMAT_FOR_LOG = "HH:mm:ss dd-MM-yyyy"
    private const val TIME_FORMAT_FOR_DIRECTORY = "HH-mm-ss_dd-MM-yyyy"
    private const val NEW_LINE = "\n"
    private const val EVROTRUST_LOG_DIRECTORY = "Evrotrust"

    private val queue = ArrayDeque<LogData>(QUEUE_CAPACITY)

    private var saveLogsToTxtFileJob: Job? = null

    @Volatile
    private var isSaveLogsToTxtFile = false

    init {
        Log.d(CURRENT_TAG, "init")
        timeDirectoryName = getCurrentTimeForDirectory()
    }

    fun logMessage(message: String, tag: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(tag, message)
            enqueue(
                LogData.DebugMessage(
                    tag = tag,
                    time = System.currentTimeMillis(),
                    message = message,
                )
            )
            saveLogsToTxtFile()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimeForDirectory(): String {
        val time = System.currentTimeMillis()
        return try {
            val date = Date(time)
            val timeString = SimpleDateFormat(TIME_FORMAT_FOR_DIRECTORY).format(date)
            Log.d(CURRENT_TAG, "getCurrentTimeForDirectory time: $time")
            timeString.ifEmpty {
                Log.e(CURRENT_TAG, "getCurrentTimeForDirectory time.ifEmpty")
                time.toString()
            }
        } catch (e: Exception) {
            Log.e(CURRENT_TAG, "getCurrentTimeForDirectory exception: ${e.message}", e)
            time.toString()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(time: Long): String {
        return try {
            val date = Date(time)
            val timeString = SimpleDateFormat(TIME_FORMAT_FOR_LOG).format(date)
            timeString.ifEmpty {
                Log.e(CURRENT_TAG, "getTime time.ifEmpty")
                time.toString()
            }
        } catch (exception: Exception) {
            Log.e(CURRENT_TAG, "getCurrentTime exception: ${exception.message}", exception)
            time.toString()
        }
    }

    private fun enqueue(message: LogData) {
        try {
            while (queue.size >= QUEUE_CAPACITY) {
                Log.d(CURRENT_TAG, "enqueue removeFirst")
                queue.removeFirst()
            }
            queue.addLast(message)
        } catch (exception: Exception) {
            Log.e(CURRENT_TAG, "enqueue exception: ${exception.message}", exception)
        }
    }

    private fun saveLogsToTxtFile() {
        if (isSaveLogsToTxtFile) return
        isSaveLogsToTxtFile = true
        saveLogsToTxtFileJob?.cancel()
        saveLogsToTxtFileJob = null
        saveLogsToTxtFileJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        .path
                val rootDirectory = File(path)
                if (!rootDirectory.exists()) {
                    val created = rootDirectory.mkdirs()
                    if (!created) {
                        Log.e(CURRENT_TAG, "Root log directory not created")
                        isSaveLogsToTxtFile = false
                        return@launch
                    }
                }
                val appDirectory = File(path, LOG_APP_FOLDER_NAME)
                if (!appDirectory.exists()) {
                    val created = appDirectory.mkdirs()
                    if (!created) {
                        Log.e(CURRENT_TAG, "App log directory not created")
                        isSaveLogsToTxtFile = false
                        return@launch
                    }
                }
                val timeDirectory = File(appDirectory, EVROTRUST_LOG_DIRECTORY)
                if (!timeDirectory.exists()) {
                    val created = timeDirectory.mkdirs()
                    if (!created) {
                        Log.e(CURRENT_TAG, "App time directory not created")
                        isSaveLogsToTxtFile = false
                        return@launch
                    }
                }
                val fileAll = File(timeDirectory, "$timeDirectoryName.txt")
                if (!fileAll.exists()) {
                    val created = fileAll.createNewFile()
                    if (!created) {
                        Log.e(CURRENT_TAG, "App log file not created")
                    }
                }

                var text: String? = buildString {
                    queue.forEach {
                        when (it) {
                            is LogData.DebugMessage -> {
                                append("${getTime(it.time)} ${it.tag} ${it.message}")
                                append(NEW_LINE)
                                append(NEW_LINE)
                            }
                            else -> {}
                        }
                    }
                }
                FileOutputStream(fileAll).use { outputStream ->
                    outputStream.write(text!!.toByteArray())
                    outputStream.flush()
                }
                Log.d(
                    CURRENT_TAG,
                    "Save logs size: ${text?.length}"
                )

                text = null
            } catch (exception: Exception) {
                Log.e(CURRENT_TAG, "saveLogsToTxtFile exception: ${exception.message}", exception)
            }
            isSaveLogsToTxtFile = false
        }
    }

}