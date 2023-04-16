package com.njbrady.nusic.profile.utils

import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.SongKeys.LikeKey
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.GeneralKeys.ERROR_KEY
import com.njbrady.nusic.utils.GeneralKeys.MESSAGE_TYPE
import com.njbrady.nusic.utils.GeneralKeys.SONG_CATEGORY
import com.njbrady.nusic.utils.GeneralTypes.BLOCKING_ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.CREATED_TYPE
import com.njbrady.nusic.utils.GeneralTypes.ERROR_TYPE
import com.njbrady.nusic.utils.GeneralTypes.UPDATE_TYPE
import com.njbrady.nusic.utils.MessageHandler
import com.njbrady.nusic.utils.OnSocketRoute
import com.njbrady.nusic.utils.SongPlayerWrapper
import org.json.JSONObject

class ProfileMessageHandler(
    private val onSongReceived: (SongPlayerWrapper, SongListType, Boolean) -> Unit,
    private val onBlockingError: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val exoMiddleMan: ExoMiddleMan,
    mainSocketHandler: MainSocketHandler
): MessageHandler(OnSocketRoute.PROFILEROUTE, mainSocketHandler) {


    override fun onMessageRecieved(jsonObject: JSONObject) {
        when (jsonObject.getString(MESSAGE_TYPE)) {
            UPDATE_TYPE -> onNewReceived(jsonObject = jsonObject)//handle song
            ERROR_TYPE -> onErrorTypeReceived(jsonObject = jsonObject)//handle error
            BLOCKING_ERROR_TYPE -> onBlockingErrorTypeReceived(jsonObject = jsonObject)
        }
    }

    private fun onNewReceived(jsonObject: JSONObject) {
        val songListType =
            if(jsonObject.get(SONG_CATEGORY) == CREATED_TYPE) {
                SongListType.Created
            } else {
                SongListType.Liked
            }
        val songPlayerWrapper = exoMiddleMan.addMedia(SongModel.fromJson(jsonObject))
        val liked = jsonObject.getBoolean(LikeKey)
        onSongReceived(songPlayerWrapper, songListType, liked)
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