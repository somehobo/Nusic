package com.njbrady.nusic.home.model
import androidx.compose.runtime.mutableStateListOf
import com.njbrady.nusic.home.responseObjects.SongObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class HomeState {
    private val _songList = MutableStateFlow<List<SongObject>>(mutableListOf())

    private val _upNow = MutableStateFlow<SongObject?>(null)
    private val _upNext = MutableStateFlow<SongObject?>(null)
    private val _upLast = MutableStateFlow<SongObject?>(null)
    private val _isLoading = MutableStateFlow(false)

    val upNow: StateFlow<SongObject?> = _upNow
    val upNext: StateFlow<SongObject?> = _upNext
    val upLast: StateFlow<SongObject?> = _upLast
    val isLoading: StateFlow<Boolean> = _isLoading



    val songList: StateFlow<List<SongObject>> = _songList

    fun clearState() {
        _upNow.value = null
        _upNext.value = null
        _upLast.value = null
        _isLoading.value = false
    }

    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun pushSongFront(song: SongObject) {

    }

    fun popSongRear() {
        _songList.update {
            it.dropLast(1)
        }
    }

}