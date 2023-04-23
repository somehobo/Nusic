package com.njbrady.nusic.upload.requests

import android.content.Context
import android.net.Uri
import com.njbrady.nusic.login.requests.LoginJsonKeys
import com.njbrady.nusic.upload.model.UploadSongRepository
import com.njbrady.nusic.upload.utils.UploadKeys
import com.njbrady.nusic.upload.utils.UploadKeys.generalError
import com.njbrady.nusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
): UploadSongRepository {
    return try {
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
            connection.doOutput
            connection.useCaches = false
            connection.addRequestProperty(
                HttpOptions.Authorization,
                localStorage.prefacedRetrieveToken()
            )
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



            // Read the response
            val responseCode = connection.responseCode

            songAudioFile.delete()
            songPhotoFile.delete()
            // Handle the response accordingly
            if (responseCode == 201) {
                // Success - process the response
                // Close the outputStream
                outputStream.flush()
                outputStream.close()
                connection.disconnect()
                UploadSongRepository()
            } else {
                // Error - handle the error
                val error = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(error)
                // Close the outputStream
                outputStream.flush()
                outputStream.close()
                connection.disconnect()

                val titleErrors: List<String>? = jsonError.optJSONArray(
                    UploadKeys.songName
                )?.toList() as List<String>?
                val songErrors: List<String>? = jsonError.optJSONArray(
                    UploadKeys.song
                )?.toList() as List<String>?
                val photoErrors: List<String>? = jsonError.optJSONArray(
                    UploadKeys.imageUrl
                )?.toList() as List<String>?
                val startErrors: List<String>? = jsonError.optJSONArray(
                    UploadKeys.start
                )?.toList() as List<String>?
                val endErrors: List<String>? = jsonError.optJSONArray(
                    UploadKeys.end
                )?.toList() as List<String>?
                val generalErrors: List<String>? =
                    jsonError.optJSONArray(LoginJsonKeys.ErrorKey)?.toList() as List<String>?
                val allErrors: List<String>? =
                    jsonError.optJSONArray(generalError)?.toList() as List<String>?


                UploadSongRepository(
                    containsError = true,
                    generalErrors = generalErrors.orEmpty().plus(allErrors.orEmpty()).ifEmpty { null },
                    songErrors = songErrors,
                    titleErrors = titleErrors,
                    timeErrors = startErrors.orEmpty().plus(endErrors.orEmpty()).ifEmpty { null },
                    songPhotoErrors = photoErrors
                )
            }
        }
    } catch (e: IOException) {
        // Handle the exception
        e.printStackTrace()
        UploadSongRepository(
            containsError = true,
            generalErrors = listOf(e.localizedMessage ?: "Something went wrong")
        )
    }
}
