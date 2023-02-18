package com.njbrady.nusic.home.utils

import android.media.MediaPlayer
import com.njbrady.nusic.home.model.SongObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


//TODO: Refactor this ugly class
class SongCardState private constructor(
    initialState: SongCardStateStates, val songObject: SongObject?
) {
    private lateinit var _mediaPlayer: MediaPlayer
    private var _playWhenReady = false
    private val _songCardStateState = MutableStateFlow(initialState)
    private val _errorMessage = MutableStateFlow("")
    private var _pauseWhenReady = false

    val songCardStateState: StateFlow<SongCardStateStates> = _songCardStateState
    val errorMessage: StateFlow<String> = _errorMessage

    constructor() : this(SongCardStateStates.Empty, null)

    constructor(
        songObject: SongObject
    ) : this(SongCardStateStates.Loading, songObject) {
        _mediaPlayer = mediaPlayerFactory(songObject)
    }

    fun retry() {
        _errorMessage.value = ""
        this.release()
        songObject?.let {
            _mediaPlayer = mediaPlayerFactory(it)
        }
    }

    fun playIfFirst() {
        if (_songCardStateState.value == SongCardStateStates.Ready || _songCardStateState.value == SongCardStateStates.Paused || !_mediaPlayer.isPlaying) {
            _playWhenReady = true
            if (!_pauseWhenReady) {
                _mediaPlayer.seekTo(0)
                _mediaPlayer.start()
                _songCardStateState.value = SongCardStateStates.Playing
            }
        } else {
            _playWhenReady = true
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

    fun pauseWhenReady() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            if (_songCardStateState.value != SongCardStateStates.Paused && _songCardStateState.value != SongCardStateStates.Loading) {
                pause()
                _pauseWhenReady = true
            } else if (_songCardStateState.value == SongCardStateStates.Loading) {
                _pauseWhenReady = true
            } else if (_songCardStateState.value == SongCardStateStates.Paused) {
                pause()
            }
        }
    }

    //used only for the purpose of avoiding a recomposition
    fun quietPauseWhenReady() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            if (_songCardStateState.value != SongCardStateStates.Paused && _songCardStateState.value != SongCardStateStates.Loading) {
                _mediaPlayer.pause()
                _pauseWhenReady = true
            } else if (_songCardStateState.value == SongCardStateStates.Loading) {
                _pauseWhenReady = true
            } else if (_songCardStateState.value == SongCardStateStates.Paused) {
                _mediaPlayer.pause()
            }
        }
    }

    fun resetForcePause() {
        _pauseWhenReady = false
    }

    fun replayFromScroll() {
        resetForcePause()
        playIfFirst()
    }

    fun resumePreviousPlayState() {
        if (songCardStateState.value != SongCardStateStates.Empty) {
            if (_pauseWhenReady) {
                _songCardStateState.value = SongCardStateStates.Playing
                _mediaPlayer.start()
            }
            _pauseWhenReady = false
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
        _mediaPlayer.release()
        _songCardStateState.value = SongCardStateStates.Playing
    }

    private fun mediaPlayerFactory(songObject: SongObject): MediaPlayer {
        _songCardStateState.value = SongCardStateStates.Loading
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(songObject.songUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            _songCardStateState.value = SongCardStateStates.Ready
            if (_playWhenReady) {
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

        mediaPlayer.setOnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> _songCardStateState.value =
                    SongCardStateStates.Loading
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> _songCardStateState.value =
                    SongCardStateStates.Playing
            }
            false
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
    Loading, Error, Ready, Playing, Paused, Completed, Empty
}