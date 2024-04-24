package com.digital.sofia.data.mappers.network.logs.request

import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.digital.sofia.data.mappers.base.BaseMapper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale

class UploadFilesRequestMapper(
    private val partName: String
): BaseMapper<List<File>, List<MultipartBody.Part>>() {

    override fun map(from: List<File>): List<MultipartBody.Part> {
        return buildList {
            from.forEach { file ->
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.toUri().toString())
                val mediaType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileExtension.lowercase(Locale.US))
                    ?.toMediaTypeOrNull()

                mediaType?.let {
                    val requestBody = file.asRequestBody(mediaType)
                    add(MultipartBody.Part.createFormData(partName, file.name, requestBody))
                }
            }
        }
    }
}