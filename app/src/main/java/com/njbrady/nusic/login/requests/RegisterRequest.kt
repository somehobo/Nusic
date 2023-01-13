package com.njbrady.nusic.login.requests

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun registerRequest(
    username: String,
    password: String,
    email: String
): String = GlobalScope.async {
    try {
        val url = URL("http://192.168.1.76/user/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val json = """{"username":"$username", "email":"$email", "password":"$password"}""".trimMargin()

        connection.outputStream.use {
            it.write(json.toByteArray())
        }

        val status = connection.responseCode

        if (status in 200..299) {
            // success
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            ""
        } else {
            // TODO: Make the response string dynamic
            "An error occurred while registering"
        }
    } catch (e: Exception) {
        return@async e.toString()
    }
}.await()