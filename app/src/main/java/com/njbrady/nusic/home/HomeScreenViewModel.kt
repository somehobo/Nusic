package com.njbrady.nusic.home

import androidx.lifecycle.ViewModel
import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    mainSocketHandler: MainSocketHandler
) : ViewModel() {

    private val _messageHandler = HomeMessageHandler(
        onSong = { songModel -> _musicCardQueue.push(songModel) },
        onError = { error ->
            _nonBlockingError.value = error
            _errorToast.value = error
        },
        onBlockingError = { error ->
            _blockingError.value = error
            _errorToast.value = error
        },
        mainSocketHandler = mainSocketHandler
    )


    private val _upNow = MutableStateFlow(SongCardState())
    private val _upNext = MutableStateFlow(SongCardState())
    private val _upLast = MutableStateFlow(SongCardState())
    private val _musicCardQueue = MusicCardStateQueue(_upNow, _upNext, _upLast)
    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _errorToast = MutableStateFlow<String?>(null)

    val upNow: StateFlow<SongCardState> = _upNow
    val upNext: StateFlow<SongCardState> = _upNext
    val upLast: StateFlow<SongCardState> = _upLast
    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _errorToast


    init {
        retry()
    }

    //add logic for empty state case
    fun likeSong(song: SongModel?, like: Boolean) {
        song?.let {
            _musicCardQueue.pop()
            runJob(LikeSongMessage(songModel = it, liked = like))
        }
    }

    fun cancelTop() {
        _musicCardQueue.pop()
        runJob(GetSongMessage())
    }

    fun likeTop(like: Boolean) {
        if (upNow.value.songCardStateState.value != SongCardStateStates.Empty) upNow.value.songObject?.let { songObject ->
            likeSong(
                songObject, like
            )
        }
    }

    fun restartTop() {
        if (upNow.value.songCardStateState.value != SongCardStateStates.Empty) upNow.value.restart()
    }

    fun retry() {
        val requestFurther = MaxQueueSize - _musicCardQueue.size()

        for (i in 0 until requestFurther) {
            runJob(GetSongMessage())
        }

    }

    fun resetToastErrors() {
        _errorToast.value = null
    }

    fun resetState() {
        _musicCardQueue.clear()
    }

    fun forcePauseCurrent() {
        upNow.value.pauseWhenReady()
    }

    fun resumeCurrentPreviousPlayState() {
        upNow.value.resumePreviousPlayState()
    }

    private fun runJob(musicMessage: MusicMessage){
        _messageHandler.sendMessage(musicMessage.getMessage())
    }

    override fun onCleared() {
        super.onCleared()
        _musicCardQueue.release()
        _messageHandler.onClear()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}


