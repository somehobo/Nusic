package com.njbrady.nusic.utils

class UrlProvider {
    companion object {
        private const val baseUrl = "http://192.168.0.179/"
        const val baseWebSocketUrl = "ws://192.168.0.179/ws/"
        const val loginUrl = baseUrl + "api-token-auth/"
        const val registerUrl = baseUrl + "user/"
        const val initialSongUrl = baseUrl + "initialrec/"
        const val feedbackUrl = baseUrl + "songfeedback/"
        const val pagedSongsUrl = baseUrl + "pagedSongs/"
        const val uploadProfilePhotoUrl = baseUrl + "postProfilePhoto/"
        const val getProfilePhotoUrl = baseUrl + "getProfilePhoto/"
        const val uploadSong = baseUrl + "songUpload/"
    }
}