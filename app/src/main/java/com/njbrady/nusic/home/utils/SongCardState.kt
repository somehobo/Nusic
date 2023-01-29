package com.njbrady.nusic.home.utils

import android.media.MediaPlayer
import com.njbrady.nusic.home.responseObjects.SongObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SongCardState private constructor(
    private val initialState: SongObjectPlayerStates,
    val songObject: SongObject?
) {


    private val _mediaPlayer = MediaPlayer()
    private var _upNow = false
    private val _songObjectPlayerState = MutableStateFlow(
        initialState
    )

    val songObjectPlayerState: StateFlow<SongObjectPlayerStates> = _songObjectPlayerState

    constructor() : this(SongObjectPlayerStates.Empty, null)

    constructor(songObject: SongObject) : this(SongObjectPlayerStates.Loading, songObject) {
        _mediaPlayer.setDataSource(songObject.songUrl)
        _mediaPlayer.prepareAsync()
        _mediaPlayer.setOnPreparedListener {
            _songObjectPlayerState.value = SongObjectPlayerStates.Ready
            if (_upNow) {
                playIfFirst()
            }
        }
        _mediaPlayer.setOnCompletionListener {
            _songObjectPlayerState.value = SongObjectPlayerStates.Completed
        }
    }


    fun playIfFirst() {
        if (_songObjectPlayerState.value == SongObjectPlayerStates.Ready) {
            _upNow = true
            if (!_mediaPlayer.isPlaying) {
                _mediaPlayer.start()
                _songObjectPlayerState.value = SongObjectPlayerStates.Playing
            }
        } else {
            _upNow = true
        }
    }

    fun pause() {
        if (songObjectPlayerState.value != SongObjectPlayerStates.Empty) {
            _songObjectPlayerState.value = SongObjectPlayerStates.Paused
            _mediaPlayer.pause()
        }
    }

    fun resume() {
        if (songObjectPlayerState.value != SongObjectPlayerStates.Empty) {
            _songObjectPlayerState.value = SongObjectPlayerStates.Playing
            _mediaPlayer.start()
        }
    }

    fun release() {
        _mediaPlayer.pause()
        _mediaPlayer.release()
    }

    companion object {
        fun orElse(i: Int): SongCardState {
            return SongCardState()
        }
    }

}

enum class SongObjectPlayerStates {
    Loading,
    Error,
    Ready,
    Playing,
    Paused,
    Completed,
    Empty
}