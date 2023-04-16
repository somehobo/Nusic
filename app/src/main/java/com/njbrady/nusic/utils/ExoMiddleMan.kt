package com.njbrady.nusic.utils

import android.content.Context
import com.njbrady.nusic.home.model.SongModel
import java.util.*


typealias TrackIndex = Int

// TODO: Handle error state for network discontinuity in next tracks

class ExoMiddleMan(context: Context) {
    private val _baseSongPlayer = BaseSongPlayer(context)
    private val _idToIndex: MutableMap<UUID, TrackIndex> = mutableMapOf()

    val currentSongPlayerState = _baseSongPlayer.exoPlayerState
    val currentSongErrorMessage = _baseSongPlayer.exoPlayerErrorMessage

    fun addMedia(songModel: SongModel): SongPlayerWrapper {
        /*
            Returns unique track identifier
            or null if error
        */
        songModel.songUrl!!.let {
            val uuid = generateUUID()
            _idToIndex[uuid] = _baseSongPlayer.appendMedia(it)
            return SongPlayerWrapper(
                songModel = songModel,
                uuid = uuid,
                play = { play(uuid) },
                remove = { removeMedia(uuid) },
                restart = { restartSong(uuid) },
                reset = { resetError(uuid, it) })
        }
    }

    fun removeMedia(uuid: UUID) {
        /*
            decrements map appropriately to maintain
            accurate track indexes
        */
        _idToIndex[uuid]?.let {
            var startDecrement = false
            _idToIndex.forEach { entry ->
                if (uuid == entry.key) {
                    startDecrement = true
                }
                if (startDecrement) {
                    _idToIndex[entry.key] = entry.value.dec()
                }
            }
            _baseSongPlayer.popMedia(it)
            _idToIndex.remove(uuid)
        }
    }

    fun resetError(uuid: UUID, url: String) {
        _idToIndex[uuid]?.let {
            _baseSongPlayer.resetErrors()
            _baseSongPlayer.popMedia(it)
            _idToIndex[uuid] = _baseSongPlayer.appendMedia(url)
            play(uuid)
        }
    }

    fun pauseCurrent() {
        _baseSongPlayer.pause()
    }

    fun play(uuid: UUID) {
        _idToIndex[uuid]?.let { _baseSongPlayer.playMedia(it) }
    }

    fun restartSong(uuid: UUID) {
        _idToIndex[uuid]?.let { _baseSongPlayer.playFromBeginning(it) }
    }

    fun release() {
        _baseSongPlayer.release()
    }

    private fun generateUUID(): UUID {
        return UUID.randomUUID()
    }

}