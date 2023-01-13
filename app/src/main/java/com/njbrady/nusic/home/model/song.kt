package com.njbrady.nusic.home.model

data class Song (
    val songName: String,
    val artistName: String,
    val artistId: Int,
    val start: Int?,
    val end: Int?,
    val songUrl: String,
    val imageUrl: String,
    val songId: Int
)