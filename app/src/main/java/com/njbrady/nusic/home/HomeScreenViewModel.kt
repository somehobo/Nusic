package com.njbrady.nusic.home

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.GetSongMessage
import com.njbrady.nusic.home.utils.HomeMessageHandler
import com.njbrady.nusic.home.utils.LikeSongMessage
import com.njbrady.nusic.home.utils.MusicMessage
import com.njbrady.nusic.upload.PlayerState
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    mainSocketHandler: MainSocketHandler,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val _exoMiddleMan: ExoMiddleMan
) : ViewModel() {

    private val _messageHandler =
        HomeMessageHandler(onSong = { songModel -> pushSongQueue(songModel) }, onError = { error ->
            _nonBlockingError.value = error
            _errorToast.value = error
        }, onBlockingError = { error ->
            _blockingError.value = error
            _errorToast.value = error
        }, mainSocketHandler = mainSocketHandler
        )

    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _errorToast = MutableStateFlow<String?>(null)
    private val _realSongQueue = MutableStateFlow(listOf<SongPlayerWrapper>())

    var tempPaused = false
    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _errorToast
    val realSongQueue: StateFlow<List<SongPlayerWrapper>> = _realSongQueue
    val topSongState: StateFlow<PlayerState> = _exoMiddleMan.currentSongPlayerState
    val topSongErrorMessage: StateFlow<String?> = _exoMiddleMan.currentSongErrorMessage
    val psd = _exoMiddleMan.psd

    init {
        retry()
    }

    private fun popSongQueue() {
        _realSongQueue.value.first().remove()
        _realSongQueue.value = _realSongQueue.value.drop(1)
        _realSongQueue.value.firstOrNull()?.play?.let { it() }
    }

    private fun pushSongQueue(songModel: SongModel) {
        viewModelScope.launch {
            withContext(dispatcher) {
                Handler(Looper.getMainLooper()).post {
                    _realSongQueue.value =
                        _realSongQueue.value.plusElement(_exoMiddleMan.addMedia(songModel))
                    _realSongQueue.value.firstOrNull()?.play?.let { it() }
                }
            }
        }
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
        realSongQueue.value.firstOrNull()?.let {
            likeSong(
                it.songModel, like
            )
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

    fun tempPauseCurrent() {
        if (topSongState.value != PlayerState.Paused) {
            _exoMiddleMan.pauseCurrent()
            tempPaused = true
        }
    }

    fun ifTempPauseThenResume() {
        if (tempPaused) {
            _realSongQueue.value.firstOrNull()?.play?.let { it() }
        }
    }

    fun pauseCurrent() {
        _exoMiddleMan.pauseCurrent()
    }

    private fun runJob(musicMessage: MusicMessage) {
        _messageHandler.sendMessage(musicMessage.getMessage())
    }

    override fun onCleared() {
        super.onCleared()
        _exoMiddleMan.release()
        _messageHandler.onClear()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}


