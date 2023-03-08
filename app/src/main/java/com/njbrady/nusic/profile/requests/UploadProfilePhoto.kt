package com.njbrady.nusic.profile.requests

import android.content.Context
import android.net.Uri
import android.util.Log
import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.ProfileKeys
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.UrlProvider
import com.njbrady.nusic.utils.getFileFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

suspend fun uploadProfilePhoto(localStorage: LocalStorage, uri: Uri, context: Context) {
    return try {
        withContext(Dispatchers.IO) {
            val uriPath = uri.toString().replace("content://", "file://")
            Log.e("UploadProfilePhoto", uri.path!!)
            Log.e("UploadProfilePhoto", uri.toString())
            Log.e("UploadProfilePhoto", uriPath)

            val file = getFileFromUri(uri = uri, context = context )
                ?: throw Exception("File not found")
            val fileData = file.readBytes()
            val url = URL(UrlProvider.uploadProfilePhotoUrl)
            val connection = url.openConnection() as HttpURLConnection
            val boundary = UUID.randomUUID().toString()
            connection.requestMethod = HttpOptions.POST
            connection.doInput = true
            connection.useCaches = false
            connection.addRequestProperty(HttpOptions.Authorization, localStorage.prefacedRetrieveToken())
            connection.addRequestProperty(
                HttpOptions.ContentType, HttpOptions.FormContentType + ";boundary=$boundary"
            )
            // Create a DataOutputStream object to write the data to the server
            val outputStream = DataOutputStream(connection.outputStream)

            // Write the boundary, the content disposition, and the file data to the output stream
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${ProfileKeys.profilePhotoKey}\"; filename=\"${file.name}\"\r\n")
            outputStream.writeBytes("\r\n")
            outputStream.write(fileData)
            outputStream.writeBytes("\r\n")
            // Write the final boundary to the output stream
            outputStream.writeBytes("--$boundary--\r\n")
            Log.e("UploadProfilePhoto", "finished uploading")

            // Close the output stream
            outputStream.flush()
            outputStream.close()
            file.delete()
            if (connection.responseCode >= 300) {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                connection.disconnect()
                throw Exception(jsonError.getString(SongKeys.ErrorKey))
            }
            connection.disconnect()
        }
    } catch (exception: Exception) {
        exception.message?.let { Log.e("UploadProfilePhoto", it) }
        throw exception
    }
}