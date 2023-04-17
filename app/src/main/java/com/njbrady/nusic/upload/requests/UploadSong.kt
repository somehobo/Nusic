package com.njbrady.nusic.upload.requests

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.njbrady.nusic.upload.utils.UploadKeys
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.UrlProvider
import com.njbrady.nusic.utils.getFileFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

suspend fun uploadSong(
    songTitle: String,
    songPhoto: Uri,
    songAudio: Uri,
    start: Int,
    end: Int,
    context: Context,
    localStorage: LocalStorage
) {
    try {
        withContext(Dispatchers.IO) {
            //create temp files
            val songPhotoFile = getFileFromUri(songPhoto, context)
                ?: throw Exception("File not found")
            val songAudioFile = getFileFromUri(songAudio, context)
                ?: throw Exception("File not found")
            // Define the API URL
            val url = URL(UrlProvider.uploadSong)
            val boundary = UUID.randomUUID().toString()

            // Establish the HttpURLConnection
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = HttpOptions.POST
            connection.doInput = true
            connection.useCaches = false
            connection.addRequestProperty(HttpOptions.Authorization, localStorage.prefacedRetrieveToken())
            connection.addRequestProperty(
                HttpOptions.ContentType, HttpOptions.FormContentType + ";boundary=$boundary"
            )
            // Create a DataOutputStream object to write the data to the server
            val outputStream = DataOutputStream(connection.outputStream)

            // Add SongTitle field
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${UploadKeys.songName}\"\r\n")
            outputStream.writeBytes("\r\n")
            outputStream.writeBytes(songTitle)
            outputStream.writeBytes("\r\n")

            // Add SongPhoto field
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${UploadKeys.imageUrl}\"; filename=\"${songPhotoFile.name}\"\r\n")
            outputStream.writeBytes("\r\n")
            val songPhotoBytes = songPhotoFile.readBytes()
            outputStream.write(songPhotoBytes)
            outputStream.writeBytes("\r\n")

            // Add SongAudio field
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${UploadKeys.song}\"; filename=\"${songAudioFile.name}\"\r\n")
            val songAudioBytes = songAudioFile.readBytes()
            outputStream.writeBytes("\r\n")
            outputStream.write(songAudioBytes)
            outputStream.writeBytes("\r\n")

            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${UploadKeys.start}\"\r\n")
            outputStream.writeBytes("\r\n")
            outputStream.writeBytes(start.toString())
            outputStream.writeBytes("\r\n")

            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"${UploadKeys.end}\"\r\n")
            outputStream.writeBytes("\r\n")
            outputStream.writeBytes(end.toString())
            outputStream.writeBytes("\r\n")


            // Finish form-data payload
            outputStream.writeBytes("--$boundary--\r\n")

            // Close the outputStream
            outputStream.flush()
            outputStream.close()

            // Read the response
            val responseCode = connection.responseCode
            val responseBody = connection.inputStream.bufferedReader().readText()

            songAudioFile.delete()
            songPhotoFile.delete()

            // Handle the response accordingly
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Success - process the response
            } else {
                // Error - handle the error
            }
            connection.disconnect()
        }
    } catch (e: IOException) {
        // Handle the exception
        e.printStackTrace()
    }

}
