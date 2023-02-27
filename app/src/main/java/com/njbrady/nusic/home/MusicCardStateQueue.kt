package com.njbrady.nusic.home

import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.SongCardState
import kotlinx.coroutines.flow.MutableStateFlow

class MusicCardStateQueue(
    private val _upNow: MutableStateFlow<SongCardState>,
    private val _upNext: MutableStateFlow<SongCardState>,
    private val _upLast: MutableStateFlow<SongCardState>
) {

    private val _realSongQueue = mutableListOf<SongCardState>()

    fun push(songObject: SongModel?) {
        songObject?.let {
            _realSongQueue.add(SongCardState(it))
        }
        setValues()
    }

    fun pop() {
        _realSongQueue.first().release()
        _realSongQueue.removeFirstOrNull()
        setValues()
    }

    fun size(): Int {
        return _realSongQueue.size
    }

    fun clear() {
        release()
        _realSongQueue.clear()
        setValues()
    }

    fun release() {
        for (songObjectPlayer in _realSongQueue) {
            songObjectPlayer.release()
        }
    }

    private fun setValues() {
        _upLast.value = getAtIndex(2)
        _upNext.value = getAtIndex(1)
        _upNow.value = getAtIndex(0)
        _upNow.value.playIfFirst()
    }

    private fun getAtIndex(index: Int): SongCardState {
        return _realSongQueue.getOrElse(index) { i -> SongCardState.orElse(i) }
    }

}