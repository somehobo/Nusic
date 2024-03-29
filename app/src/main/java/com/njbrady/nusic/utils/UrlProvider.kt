package com.njbrady.nusic.utils

class UrlProvider {
    companion object {
        private const val baseUrl = "http://76.127.69.153:3/"
        const val baseWebSocketUrl = "ws://76.127.69.153:3/ws/"
        const val loginUrl = baseUrl + "api-token-auth/"
        const val registerUrl = baseUrl + "user/"
        const val initialSongUrl = baseUrl + "initialrec/"
        const val feedbackUrl = baseUrl + "songfeedback/"
        const val pagedSongsUrl = baseUrl + "pagedSongs/"
        const val uploadProfilePhotoUrl = baseUrl + "postProfilePhoto/"
        const val getUserAttributesUrl = baseUrl + "userAttributes/"
        const val postUserBioUrl = baseUrl + "postUserBio/"
        const val uploadSong = baseUrl + "songUpload/"
    }
}