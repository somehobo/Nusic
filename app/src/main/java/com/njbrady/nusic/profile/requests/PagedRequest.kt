package com.njbrady.nusic.profile.requests

import android.util.Log
import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.PagedResponse
import com.njbrady.nusic.profile.utils.ProfileKeys
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun pagedRequest(tokenStorage: TokenStorage, page: Int): PagedResponse {
    return try {
        withContext(Dispatchers.IO) {
            Log.e("PagedRequest", "SENDING REQUEST")
            val url = URL(UrlProvider.likedSongsPagedUrl)
            Log.e("PagedRequest", UrlProvider.likedSongsPagedUrl)

            val connection = url.openConnection() as HttpURLConnection
            Log.e("PagedRequest", "opening connection")
            connection.requestMethod = HttpOptions.POST
            connection.doOutput = true
            connection.addRequestProperty(HttpOptions.Authorization, tokenStorage.retrieveToken())
            connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)
            val toSend =
                mapOf(ProfileKeys.pageKey to page)

            val toSendJson = JSONObject(toSend).toString()

            Log.e("PagedRequest", "sending: $toSendJson")


            connection.outputStream.use {
                Log.e("PagedRequest", "writing!)")
                it.write(toSendJson.toByteArray())
            }
            Log.e("PagedRequest", connection.responseCode.toString())


            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                Log.e("PagedRequest", jsonResponse.toString())
                PagedResponse.fromJson(jsonResponse)
            } else {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                throw Exception(jsonError.getString(SongKeys.ErrorKey))
            }
        }
    } catch (exception: Exception) {
        exception.message?.let {
            Log.e("PagedRequest", it)
        }
        throw exception
    }
}