package com.njbrady.nusic.utils

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer

class ExoPlayerPositionTracker(
    private val mediaPlayer: ExoPlayer,
    private val onPositionChanged: (position: Int) -> Unit,
    private val updateIntervalMillis: Long = 500
) {
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition: Long = mediaPlayer.currentPosition / 1000
//                Log.e("media player tracker", currentPosition.toString())
                onPositionChanged(currentPosition.toInt())
            }
            handler.postDelayed(this, updateIntervalMillis)
        }
    }

    fun startTracking() {
        handler.postDelayed(runnable, updateIntervalMillis)
    }

    fun stopTracking() {
        handler.removeCallbacks(runnable)
    }
}
