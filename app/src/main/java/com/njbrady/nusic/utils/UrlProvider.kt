package com.njbrady.nusic.utils

class UrlProvider {
    companion object {
        val baseUrl = "http://192.168.1.76/"
        val loginUrl = baseUrl + "api-token-auth/"
        val registerUrl = baseUrl + "user/"
    }
}