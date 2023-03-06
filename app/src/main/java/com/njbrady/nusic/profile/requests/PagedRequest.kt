package com.njbrady.nusic.profile.requests

import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.PagedResponse
import com.njbrady.nusic.profile.utils.ProfileKeys
import com.njbrady.nusic.profile.utils.ProfileValues
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class Type {Liked, Created}

suspend fun pagedRequest(tokenStorage: TokenStorage, page: Int, type: Type): PagedResponse {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(UrlProvider.pagedSongsUrl)
            val listType = if (type == Type.Liked) ProfileValues.likedListValue else ProfileValues.createdistValue
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = HttpOptions.POST
            connection.doOutput = true
            connection.addRequestProperty(HttpOptions.Authorization, tokenStorage.prefacedRetrieveToken())
            connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)
            val toSend =
                mapOf(ProfileKeys.pageKey to page, ProfileKeys.songListType to listType)

            val toSendJson = JSONObject(toSend).toString()

            connection.outputStream.use {
                it.write(toSendJson.toByteArray())
            }

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                PagedResponse.fromJson(jsonResponse)
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