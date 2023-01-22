package com.njbrady.nusic.home

import com.njbrady.nusic.home.responseObjects.SongObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.LinkedList
import java.util.Queue

class MusicCardQueue(
    private val _upNow: MutableStateFlow<SongObject?>,
    private val _upNext: MutableStateFlow<SongObject?>,
    private val _upLast: MutableStateFlow<SongObject?>
) {

    private val _realSongQueue = mutableListOf<SongObject>()


    fun push(songObject: SongObject?) {
        songObject?.let {
            _realSongQueue.add(it)
        }
        _upNow.value = _realSongQueue.getOrNull(0)
        _upNext.value = _realSongQueue.getOrNull(1)
        _upLast.value = _realSongQueue.getOrNull(2)
    }

    fun pop() {
        _realSongQueue.removeFirstOrNull()
        _upNow.value = _realSongQueue.getOrNull(0)
        _upNext.value = _realSongQueue.getOrNull(1)
        _upLast.value = _realSongQueue.getOrNull(2)
    }

    fun size(): Int {
        return _realSongQueue.size
    }

    fun clear() {
        _realSongQueue.clear()
        _upNow.value = _realSongQueue.getOrNull(0)
        _upNext.value = _realSongQueue.getOrNull(1)
        _upLast.value = _realSongQueue.getOrNull(2)
    }

}