package com.njbrady.nusic.home.utils

import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.utils.GeneralKeys.ERROR_KEY
import com.njbrady.nusic.utils.GeneralKeys.MESSAGE_TYPE
import com.njbrady.nusic.utils.GeneralTypes.BLOCKING_ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.SONG_TYPE
import com.njbrady.nusic.utils.MessageHandler
import com.njbrady.nusic.utils.OnSocketRoute
import org.json.JSONObject

class HomeMessageHandler(
    private val onSong: (SongModel) -> Unit,
    private val onBlockingError: (String) -> Unit,
    private val onError: (String) -> Unit,
    mainSocketHandler: MainSocketHandler
): MessageHandler(OnSocketRoute.HOMEROUTE, mainSocketHandler) {

    override fun onMessageRecieved(jsonObject: JSONObject) {
        when (jsonObject.getString(MESSAGE_TYPE)) {
            SONG_TYPE -> onSongTypeReceived(jsonObject = jsonObject)
            ERROR_TYPE -> onErrorTypeReceived(jsonObject = jsonObject)
            BLOCKING_ERROR_TYPE -> onBlockingErrorTypeReceived(jsonObject = jsonObject)
        }
    }

    private fun onSongTypeReceived(jsonObject: JSONObject) {
        val receivedSong = SongModel.fromJson(jsonObject)
        onSong(receivedSong)
    }

    private fun onErrorTypeReceived(jsonObject: JSONObject) {
        val receivedError = jsonObject.getString(ERROR_KEY)
        onError(receivedError)
    }

    private fun onBlockingErrorTypeReceived(jsonObject: JSONObject) {
        val receivedError = jsonObject.getString(ERROR_KEY)
        onBlockingError(receivedError)
    }

}