package com.njbrady.nusic.home.responseObjects

import org.json.JSONObject

class SongObject(
    val name: String? = null,
    val artist: String? = null,
    val start: Int? = null,
    val end: Int? = null,
    val songUrl: String? = null,
    val imageUrl: String? = null,
    val songId: Int? = null,
    val artistId: Int? = null,
) {
    companion object Factory {
        private const val keyName = "songName"
        private const val keyArtist = "artist"
        private const val keyStart = "start"
        private const val keyEnd = "end"
        private const val keySongUrl = "song"
        private const val keyImageUrl = "image"
        private const val keySongId = "id"
        private const val keyArtistId = "artistId"

        fun fromJson(json: JSONObject): SongObject {
            try {
                return SongObject(
                    name = nonEmptyOrNull(json.optString(keyName)),
                    artist = nonEmptyOrNull(json.optString(keyArtist)),
                    start = nonNegOrNull(json.optInt(keyStart, -1)),
                    end = nonNegOrNull(json.optInt(keyEnd, -1)),
                    songUrl = nonEmptyOrNull(json.optString(keySongUrl)),
                    imageUrl = nonEmptyOrNull(json.optString(keyImageUrl)),
                    songId = nonNegOrNull(json.optInt(keySongId, -1)),
                    artistId = nonNegOrNull(json.optInt(keyArtistId, -1)),
                )
            } catch (e: Exception) {
                throw Exception("Key missmatch")
            }
        }

        private fun nonEmptyOrNull(s: String): String? {
            return if (s == "") {
                null
            } else {
                s
            }
        }

        private fun nonNegOrNull(i: Int): Int? {
            return if (i < 0) {
                null
            } else {
                i
            }
        }
    }
}