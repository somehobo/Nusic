package com.njbrady.nusic.login.requests

import com.njbrady.nusic.login.model.LoginRepository
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider
import com.njbrady.nusic.utils.toList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


suspend fun loginRequest(
    username: String,
    password: String,
    tokenStorage: TokenStorage
): LoginRepository = GlobalScope.async {
    try {
        val url = URL(UrlProvider.loginUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = HttpOptions.POST
        connection.doOutput = true
        connection.setRequestProperty(HttpOptions.ContentType, HttpOptions.JsonContentType)

        val toSend =
            mapOf(LoginJsonKeys.UserNameKey to username, LoginJsonKeys.PasswordKey to password)
        val toSendJson = JSONObject(toSend).toString()

        connection.outputStream.use {
            it.write(toSendJson.toByteArray())
        }
        val status = connection.responseCode

        if (status in 200..299) {
            // success
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            val token = jsonResponse.getString(LoginJsonKeys.TokenKey)
            tokenStorage.storeToken(token)
            LoginRepository()
        } else {
            val error = connection.errorStream.bufferedReader().use { it.readText() }
            val jsonError = JSONObject(error)

            val usernameErrors: List<String>? =
                jsonError.optJSONArray(LoginJsonKeys.UserNameKey)?.toList() as List<String>?
            val passwordErrors: List<String>? =
                jsonError.optJSONArray(LoginJsonKeys.PasswordKey)?.toList() as List<String>?
            val generalErrors: List<String>? =
                jsonError.optJSONArray(LoginJsonKeys.ErrorKey)?.toList() as List<String>?

            LoginRepository(
                usernameError = usernameErrors.orEmpty(),
                passwordError = passwordErrors.orEmpty(),
                errorMessages = generalErrors.orEmpty(),
                containsError = true
            )
        }
    } catch (e: Exception) {
        return@async LoginRepository(errorMessages = listOf(e.toString()), containsError = true)
    }
}.await()