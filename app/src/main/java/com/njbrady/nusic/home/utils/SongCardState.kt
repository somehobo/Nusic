package com.njbrady.nusic.home.utils

import android.media.MediaPlayer
import com.njbrady.nusic.home.responseObjects.SongObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SongCardState private constructor(
    initialState: SongCardStateStates,
    val songObject: SongObject?
) {

    constructor() : this(SongCardStateStates.Empty, null)

    constructor(songObject: SongObject) : this(SongCardStateStates.Loading, songObject) {
        _mediaPlayer = mediaPlayerFactory(songObject)
    }

    private var _mediaPlayer = MediaPlayer()
    private var _upNow = false
    private val _songCardStateState = MutableStateFlow(initialState)
    private val _errorMessage = MutableStateFlow("")

    val songCardStateState: StateFlow<SongCardStateStates> = _songCardStateState
    val errorMessage: StateFlow<String> = _errorMessage

    fun retry() {
        _errorMessage.value = ""
        this.release()
        songObject?.let {
            _mediaPlayer = mediaPlayerFactory(it)
        }
    }

    fun playIfFirst() {
        if (_songCardStateState.value == SongCardStateStates.Ready) {
            _upNow = true
            if (!_mediaPlayer.isPlaying) {
                _mediaPlayer.start()
                _songCardStateState.value = SongCardStateStates.Playing
            }
        } else {
            _upNow = true
        }
    }

    fun pause() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            _songCardStateState.value = SongCardStateStates.Paused
            _mediaPlayer.pause()
        }
    }

    fun resume() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            _songCardStateState.value = SongCardStateStates.Playing
            _mediaPlayer.start()
        }
    }

    fun restart() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            _songCardStateState.value = SongCardStateStates.Playing
            _mediaPlayer.seekTo(0)
            _mediaPlayer.start()
        }
    }

    fun release() {
        _mediaPlayer.pause()
        _mediaPlayer.release()
    }

    fun clearVisibleState() {
        _songCardStateState.value = SongCardStateStates.Empty
    }

    private fun mediaPlayerFactory(songObject: SongObject): MediaPlayer {
        _songCardStateState.value = SongCardStateStates.Loading
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(songObject.songUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            _songCardStateState.value = SongCardStateStates.Ready
            if (_upNow) {
                playIfFirst()
            }
        }

        mediaPlayer.setOnCompletionListener {
            _songCardStateState.value = SongCardStateStates.Completed
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            mp.pause()
            when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                    // Handle unknown error
                    _songCardStateState.value = SongCardStateStates.Error
                    _errorMessage.value = "An unknown error occurred"
                }

                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    // Handle server died error
                    mp.release()
                    _mediaPlayer = mediaPlayerFactory(songObject)
                }
            }
            true // Return true to indicate the error has been handled
        }
        return mediaPlayer
    }

    companion object {
        private val emptySongCardState = SongCardState()
        fun orElse(i: Int): SongCardState {
            return emptySongCardState
        }
    }

}

enum class SongCardStateStates {
    Loading,
    Error,
    Ready,
    Playing,
    Paused,
    Completed,
    Empty
}