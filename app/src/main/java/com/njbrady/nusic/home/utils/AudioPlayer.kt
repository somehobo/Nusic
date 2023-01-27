package com.njbrady.nusic.home.utils

import android.media.MediaPlayer

class AudioPlayer {
    private val _mediaPlayer = MediaPlayer()

    var currentSongUrl = ""

    fun pause() {
        _mediaPlayer.pause()
    }

    fun play() {
        _mediaPlayer.start()
    }

    fun setSong(newSongUrl: String) {
        _mediaPlayer.pause()
        currentSongUrl = newSongUrl
        _mediaPlayer.setDataSource(newSongUrl)
        _mediaPlayer.prepareAsync()
        _mediaPlayer.setOnPreparedListener {
            _mediaPlayer.start()
        }
    }
}