package com.njbrady.nusic.profile.requests

import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.profile.utils.PagedResponse
import com.njbrady.nusic.profile.utils.ProfileKeys
import com.njbrady.nusic.profile.utils.ProfileValues
import com.njbrady.nusic.utils.GeneralKeys.USERIDKEY
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.UrlProvider
import com.njbrady.nusic.utils.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class SongListType { Liked, Created }

suspend fun pagedRequest(
    localStorage: LocalStorage,
    page: Int,
    type: SongListType,
    otherUserModel: UserModel? = null
): PagedResponse {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(UrlProvider.pagedSongsUrl)
            val listType =
                if (type == SongListType.Liked) ProfileValues.likedListValue else ProfileValues.createdistValue
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = HttpOptions.POST
            connection.doOutput = true
            connection.addRequestProperty(
                HttpOptions.Authorization,
                localStorage.prefacedRetrieveToken()
            )
            connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)
            val toSend =
                mapOf(
                    ProfileKeys.pageKey to page,
                    ProfileKeys.songListType to listType,
                    USERIDKEY to (otherUserModel?.id ?: localStorage.retrieveUserModel().id)
                )

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