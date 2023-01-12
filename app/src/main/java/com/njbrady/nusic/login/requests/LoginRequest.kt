package com.njbrady.nusic.login.requests

import com.njbrady.nusic.login.data.TokenStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


suspend fun loginRequest(
    username: String,
    password: String,
    tokenStorage: TokenStorage
): String = GlobalScope.async {
    try {
        val url = URL("http://192.168.1.76/api-token-auth/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val json = """{"username":"$username","password":"$password"}"""

        connection.outputStream.use {
            it.write(json.toByteArray())
        }

        val status = connection.responseCode

        if (status in 200..299) {
            // success
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            val token = jsonResponse.getString("token")
            tokenStorage.storeToken(token)
            ""
        } else {
            // TODO: Make the response string dynamic
            "An error occurred while loging on"
        }
    } catch (e: Exception) {
        return@async e.toString()
    }
}.await()