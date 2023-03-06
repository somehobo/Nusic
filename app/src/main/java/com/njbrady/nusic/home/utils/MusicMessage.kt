package com.njbrady.nusic.home.utils

import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.SongKeys.LikeKey
import com.njbrady.nusic.utils.GeneralKeys.MESSAGE_TYPE
import com.njbrady.nusic.utils.GeneralKeys.SONG_ID
import org.json.JSONObject


abstract class MusicMessage() {
    abstract val map: Map<String,Any?>
    abstract fun getMessage(): JSONObject
}

class GetSongMessage() : MusicMessage() {
    override val map = mapOf(MESSAGE_TYPE to GET_SONG_TYPE)

    override fun getMessage(): JSONObject {
        return JSONObject(map)
    }

    companion object {
        const val GET_SONG_TYPE = "get_song"
    }
}

class LikeSongMessage(private val songModel: SongModel, private val liked: Boolean): MusicMessage() {
    override val map = mapOf(MESSAGE_TYPE to FEEDBACK_TYPE, SONG_ID to songModel.songId, LikeKey to liked)

    override fun getMessage(): JSONObject {
        return JSONObject(map)
    }
    companion object {
        const val FEEDBACK_TYPE = "feedback"
    }
}
