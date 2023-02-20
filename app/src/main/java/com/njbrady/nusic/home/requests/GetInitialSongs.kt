package com.njbrady.nusic.home.requests

import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.model.SongObjectErrorWrapper
import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

fun getSong(
    tokenStorage: TokenStorage
): SongObjectErrorWrapper {
    try {
        val url = URL(UrlProvider.initialSongUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = HttpOptions.GET
        connection.doOutput = false
        connection.addRequestProperty(HttpOptions.Authorization, tokenStorage.retrieveToken())
        connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)
        return when (connection.responseCode) {
            in 200..299 -> {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                SongObjectErrorWrapper(songObject = SongModel.fromJson(jsonResponse))
            }
            418 -> {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                SongObjectErrorWrapper(nonBlockingError = jsonError.getString(SongKeys.ErrorKey))
            }
            else -> {
                val blockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(blockingError)
                SongObjectErrorWrapper(blockingError = jsonError.getString(SongKeys.ErrorKey))
            }
        }

    } catch (e: Exception) {
        return SongObjectErrorWrapper(blockingError = e.message)
    }
}

fun likeSong(
    songObject: SongModel,
    liked: Boolean,
    tokenStorage: TokenStorage
): SongObjectErrorWrapper {
    try {

        val url = URL(UrlProvider.feedbackUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = HttpOptions.POST
        connection.doOutput = true
        connection.addRequestProperty(HttpOptions.Authorization, tokenStorage.retrieveToken())
        connection.addRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)

        val toSend =
            mapOf(SongKeys.SongKey to songObject.songId, SongKeys.LikeKey to liked)
        val toSendJson = JSONObject(toSend).toString()

        connection.outputStream.use {
            it.write(toSendJson.toByteArray())
        }
        return when (connection.responseCode) {
            in 200..299 -> {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                SongObjectErrorWrapper(songObject = SongModel.fromJson(jsonResponse))
            }
            418 -> {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                SongObjectErrorWrapper(nonBlockingError = jsonError.getString(SongKeys.ErrorKey))
            }
            else -> {
                val blockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(blockingError)
                SongObjectErrorWrapper(blockingError = jsonError.getString(SongKeys.ErrorKey))
            }
        }
    } catch (e: Exception) {
        return SongObjectErrorWrapper(blockingError = e.message)
    }
}