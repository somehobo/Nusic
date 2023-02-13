package com.njbrady.nusic.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.home.model.SongObject
import com.njbrady.nusic.home.utils.*
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject

/*
known bugs:
- Sometimes a duplicate music card appears
- On empty screen flash of old card
- need loading state in the middle
 */

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _jobRunner = JobRunner(
        scope = viewModelScope,
        onRecieved = { songObjectErrorWrapper -> _musicCardQueue.push(songObjectErrorWrapper.songObject) },
        defaultDispatcher = defaultDispatcher,
        tokenStorage = tokenStorage
    )
    private val _upNow = MutableStateFlow(SongCardState())
    private val _upNext = MutableStateFlow(SongCardState())
    private val _upLast = MutableStateFlow(SongCardState())
    private val _musicCardQueue = MusicCardStateQueue(_upNow, _upNext, _upLast)
    private val _jobQueue: Queue<MusicJob> = LinkedList()

    val upNow: StateFlow<SongCardState> = _upNow
    val upNext: StateFlow<SongCardState> = _upNext
    val upLast: StateFlow<SongCardState> = _upLast
    val isLoading: StateFlow<Boolean> = _jobRunner.isLoading
    val nonBlockingError: StateFlow<String?> = _jobRunner.nonBlockingError
    val blockingError: StateFlow<String?> = _jobRunner.blockingError
    val blockingErrorToast: StateFlow<String?> = _jobRunner.blockingErrorToast


    init {
        retry()
    }

    //add logic for empty state case
    fun likeSong(song: SongObject?, like: Boolean) {
        song?.let {
            _musicCardQueue.pop()
            _jobRunner.enqueueJob(LikeSongJob(it, like, tokenStorage))
        }
    }

    fun cancelTop() {
        _musicCardQueue.pop()
        _jobRunner.enqueueJob(GetSongJob(tokenStorage))
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
        _jobRunner.retry(_musicCardQueue.size())
    }

    fun resetToastErrors() {
        _jobRunner.resetToastErrors()
    }

    fun resetState() {
        _jobQueue.clear()
        _musicCardQueue.clear()
        _jobRunner.resetState()
    }

    fun forcePauseCurrent() {
        upNow.value.pauseWhenReady()
    }

    fun resumeCurrentPreviousPlayState() {
        upNow.value.resumePreviousPlayState()
    }

    override fun onCleared() {
        super.onCleared()
        _musicCardQueue.release()
        _jobRunner.release()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}


