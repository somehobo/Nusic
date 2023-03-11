package com.njbrady.nusic.profile.utils

import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.SongCardState
import com.njbrady.nusic.home.utils.SongKeys.LikeKey
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.utils.GeneralKeys.ERROR_KEY
import com.njbrady.nusic.utils.GeneralKeys.MESSAGE_TYPE
import com.njbrady.nusic.utils.GeneralKeys.SONG_CATEGORY
import com.njbrady.nusic.utils.GeneralTypes.BLOCKING_ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.CREATED_TYPE
import com.njbrady.nusic.utils.GeneralTypes.ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.UPDATE_TYPE
import com.njbrady.nusic.utils.OnSocketRoute
import org.json.JSONObject

class ProfileMessageHandler(
    private val onSongReceived: (SongCardState, SongListType, Boolean) -> Unit,
    private val onBlockingError: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val mainSocketHandler: MainSocketHandler
) {

    init {
        mainSocketHandler.subscribeNewRoute(
            route = OnSocketRoute.PROFILEROUTE
        ) { jsonObject -> onMessageRecieved(jsonObject = jsonObject) }
    }

    private fun onMessageRecieved(jsonObject: JSONObject) {
        when (jsonObject.getString(MESSAGE_TYPE)) {
            UPDATE_TYPE -> onNewReceived(jsonObject = jsonObject)//handle song
            ERROR_TYPE -> onErrorTypeReceived(jsonObject = jsonObject)//handle error
            BLOCKING_ERROR_TYPE -> onBlockingErrorTypeReceived(jsonObject = jsonObject)
        }
    }

    fun sendMessage(jsonObject: JSONObject) {
        mainSocketHandler.send(jsonObject)
    }

    private fun onNewReceived(jsonObject: JSONObject) {
        val songListType =
            if(jsonObject.get(SONG_CATEGORY) == CREATED_TYPE) {
                SongListType.Created
            } else {
                SongListType.Liked
            }
        val songCardState = SongCardState(SongModel.fromJson(jsonObject))
        val liked = jsonObject.getBoolean(LikeKey)
        onSongReceived(songCardState, songListType, liked)
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