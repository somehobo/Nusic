package com.njbrady.nusic.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

fun getFileFromUri(uri: Uri, context: Context): File? {
    val contentResolver = context.contentResolver
    val inputStream =
        contentResolver.openInputStream(uri) ?: // Failed to open input stream, return null
        return null
    val mimeType = contentResolver.getType(uri)

    val file = uri.lastPathSegment?.let {
        File(
            context.cacheDir,
            it + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        )
    }
    val outputStream = FileOutputStream(file)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return file
}
