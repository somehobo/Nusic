package com.njbrady.nusic.utils

class UrlProvider {
    companion object {
        const val baseUrl = "http://192.168.0.179/"
        const val loginUrl = baseUrl + "api-token-auth/"
        const val registerUrl = baseUrl + "user/"
        const val initialSongUrl = baseUrl + "initialrec/"
        const val feedbackUrl = baseUrl + "songfeedback/"
        const val likedSongsPagedUrl = baseUrl + "pagedLikedSongs/"
        const val createdSongsPagedUrl = baseUrl + "pagedCreatedSongs/"

    }
}