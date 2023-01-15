package com.njbrady.nusic.home.responseObjects

import org.json.JSONObject

class SongObject(
    val name: String,
    val artist: String,
    val start: Int,
    val end: Int,
    val songUrl: String,
    val imageUrl: String,
    val songId: Int,
    val artistId: Int
) {
    companion object Factory {
        private const val keyName = "songName"
        private const val keyArtist = "artist"
        private const val keyStart = "start"
        private const val keyEnd = "end"
        private const val keySongUrl = "song"
        private const val keyImageUrl = "image"
        private const val keySongId = "songId"
        private const val keyArtistId = "artistId"

        fun fromJson(json: JSONObject): SongObject {
            try {
                return SongObject(
                    name = json.getString(keyName),
                    artist = json.getString(keyArtist),
                    start = json.getInt(keyStart),
                    end = json.getInt(keyEnd),
                    songUrl = json.getString(keySongUrl),
                    imageUrl = json.getString(keyImageUrl),
                    songId = json.getInt(keySongId),
                    artistId = json.getInt(keyArtistId)
                )
            } catch (e: Exception) {
                throw Exception("Key missmatch")
            }
        }
    }
}