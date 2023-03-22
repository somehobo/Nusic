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
        onSong = { songModel -> pushSongQueue(songModel) },
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


    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _errorToast = MutableStateFlow<String?>(null)
    private val _realSongQueue = MutableStateFlow(listOf<SongCardState>())

    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _errorToast
    val realSongQueue: StateFlow<List<SongCardState>> = _realSongQueue

    init {
        retry()
    }

    private fun popSongQueue() {
        _realSongQueue.value.first().release()
        _realSongQueue.value = _realSongQueue.value.drop(1)
        _realSongQueue.value.firstOrNull()?.playIfFirst()
    }

    private fun pushSongQueue(songModel: SongModel) {
        _realSongQueue.value = _realSongQueue.value.plusElement(SongCardState(songModel))
        _realSongQueue.value.firstOrNull()?.playIfFirst()
    }

    //add logic for empty state case
    fun likeSong(song: SongModel?, like: Boolean) {
        song?.let {
            popSongQueue()
            runJob(LikeSongMessage(songModel = it, liked = like))
        }
    }

    fun cancelTop() {
        popSongQueue()
        runJob(GetSongMessage())
    }

    fun likeTop(like: Boolean) {
        with(realSongQueue.value.firstOrNull()) {
            if (this?.songCardStateState?.value != SongCardStateStates.Empty) this?.songObject?.let { songObject ->
                likeSong(
                    songObject, like
                )
            }
        }
    }

    fun restartTop() {
        with(realSongQueue.value.firstOrNull()) {
            if (this?.songCardStateState?.value != SongCardStateStates.Empty) this?.restart()
        }
    }

    fun retry() {
        val requestFurther = MaxQueueSize - _realSongQueue.value.size

        for (i in 0 until requestFurther) {
            runJob(GetSongMessage())
        }

    }

    fun resetToastErrors() {
        _errorToast.value = null
    }

    fun resetState() {
        _realSongQueue.value.forEach {
            it.release()
        }
    }

    fun forcePauseCurrent() {
        with(realSongQueue.value.firstOrNull()) {
            this?.pauseWhenReady()
        }
    }

    fun resumeCurrentPreviousPlayState() {
        with(realSongQueue.value.firstOrNull()) {
            this?.resumePreviousPlayState()
        }
    }

    private fun runJob(musicMessage: MusicMessage){
        _messageHandler.sendMessage(musicMessage.getMessage())
    }

    override fun onCleared() {
        super.onCleared()
        _realSongQueue.value.forEach {
            it.release()
        }
        _messageHandler.onClear()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}


