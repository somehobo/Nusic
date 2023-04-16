package com.njbrady.nusic.utils

import android.content.Context
import com.google.android.exoplayer2.*
import com.njbrady.nusic.upload.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BaseSongPlayer(context: Context) {
    private val _exoPlayer = ExoPlayer.Builder(context).setPauseAtEndOfMediaItems(true).build()
    private val _exoPlayerState = MutableStateFlow(PlayerState.Loading)
    private val _exoPlayerErrorMessage = MutableStateFlow<String?>(null)

    val exoPlayerState: StateFlow<PlayerState> = _exoPlayerState
    val exoPlayerErrorMessage: StateFlow<String?> = _exoPlayerErrorMessage

    init {
        _exoPlayer.addListener(object : Player.Listener {
            var tracksChanging = false
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        tracksChanging = false
                        _exoPlayerState.value =
                            PlayerState.Playing
                    }
                    Player.STATE_BUFFERING -> {
                        if(!tracksChanging)
                            _exoPlayerState.value = PlayerState.Loading
                    }
                    Player.STATE_ENDED -> {
                        _exoPlayerState.value = PlayerState.Completed
                    }
                    else -> {}
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                tracksChanging = !tracksChanging
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                _exoPlayerState.value = PlayerState.Error
                _exoPlayerErrorMessage.value = error.message
            }
        })
    }

    fun appendMedia(url: String): Int {
        _exoPlayer.addMediaItem(MediaItem.fromUri(url))
        if (_exoPlayer.mediaItemCount > 0) {
            _exoPlayer.prepare()
        }
        return _exoPlayer.mediaItemCount - 1
    }

    fun popMedia(index: Int) {
        _exoPlayer.removeMediaItem(index)
    }

    fun resetErrors() {
        _exoPlayerErrorMessage.value = null
        _exoPlayerState.value = PlayerState.Playing
    }

    fun playMedia(index: Int) {
        if (_exoPlayer.currentMediaItemIndex == index) {
            _exoPlayerState.value = PlayerState.Playing
            _exoPlayer.play()
        } else {
            playFromBeginning(index)
        }
    }

    fun playFromBeginning(index: Int) {
        _exoPlayerState.value = PlayerState.Playing
        _exoPlayer.seekTo(index, 0)
        _exoPlayer.play()
    }

    fun pause() {
        _exoPlayer.pause()
        _exoPlayerState.value = PlayerState.Paused
    }

    fun release() {
        _exoPlayer.release()
    }
}