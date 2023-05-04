package com.njbrady.nusic.profile.requests

import android.util.Log
import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.UploadBioErrorModel
import com.njbrady.nusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun uploadBio(localStorage: LocalStorage, bio: String): UploadBioErrorModel? {
    return try {
        Log.e("UploadBio", "First")
        val url = URL(UrlProvider.postUserBioUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = HttpOptions.POST
        connection.doOutput = true
        connection.setRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)
        connection.addRequestProperty(
            HttpOptions.Authorization,
            localStorage.prefacedRetrieveToken()
        )
        val toSend =
            mapOf(
                GeneralKeys.BIO to bio
            )

        val toSendJson = JSONObject(toSend).toString()
        connection.outputStream.use {
            it.write(toSendJson.toByteArray())
        }

        if (connection.responseCode > 300) {
            val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
            val jsonError = JSONObject(nonBlockingError)
            Log.e("UploadBio", jsonError.toString())

            UploadBioErrorModel.fromJson(jsonError)

        } else {
            null
        }

    } catch (exception: Exception) {
        throw exception
    }
}
