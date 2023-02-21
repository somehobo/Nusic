package com.njbrady.nusic.profile.requests

import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.ProfileKeys
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun getProfilePhoto(tokenStorage: TokenStorage): String {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(UrlProvider.getProfilePhotoUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = HttpOptions.GET
            connection.doOutput = false
            connection.addRequestProperty(HttpOptions.Authorization, tokenStorage.retrieveToken())
            connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)

            if (connection.responseCode in 200.. 300) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getString(ProfileKeys.profilePhotoKey)
            } else {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                throw Exception(jsonError.getString(SongKeys.ErrorKey))
            }
        }
    } catch (exception: Exception) {
        throw exception
    }
}