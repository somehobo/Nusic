package com.njbrady.nusic.home.model

import com.njbrady.nusic.utils.GeneralKeys.USERKEY
import com.njbrady.nusic.utils.UserModel
import org.json.JSONObject

data class SongModel(
    val name: String,
    val userModel: UserModel,
    val start: Int,
    val end: Int,
    val songUrl: String,
    val imageUrl: String,
    val songId: Int,
) {
    companion object Factory {
        private const val keyName = "songName"
        private const val keyUser = USERKEY
        private const val keyStart = "start"
        private const val keyEnd = "end"
        private const val keySongUrl = "song"
        private const val keyImageUrl = "imageUrl"
        private const val keySongId = "id"

        fun fromJson(json: JSONObject): SongModel {
            try {
                return SongModel(
                    name = json.getString(keyName),
                    userModel = UserModel.fromJson(json.getJSONObject(keyUser)),
                    start = json.getInt(keyStart),
                    end = json.getInt(keyEnd),
                    songUrl = json.getString(keySongUrl),
                    imageUrl = json.getString(keyImageUrl),
                    songId = json.getInt(keySongId),
                )
            } catch (e: Exception) {
                throw Exception("Key missmatch")
            }
        }
    }
}