package com.njbrady.nusic.profile.requests

import com.njbrady.nusic.home.utils.SongKeys
import com.njbrady.nusic.home.utils.SongKeys.ErrorKey
import com.njbrady.nusic.login.requests.LoginJsonKeys
import com.njbrady.nusic.utils.*
import com.njbrady.nusic.utils.GeneralKeys.USERIDKEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun getUserAttributes(localStorage: LocalStorage, userModel: UserModel): UserAttributes {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(UrlProvider.getUserAttributesUrl)
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
                    USERIDKEY to userModel.id
                )

            val toSendJson = JSONObject(toSend).toString()
            connection.outputStream.use {
                it.write(toSendJson.toByteArray())
            }
            if (connection.responseCode in 200..300) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                UserAttributes.fromJson(jsonResponse)
            } else {
                val nonBlockingError = connection.errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(nonBlockingError)
                throw Exception(jsonError.getString(ErrorKey))
            }
        }
    } catch (exception: Exception) {
        throw exception
    }
}
