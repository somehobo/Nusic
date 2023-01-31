package com.njbrady.nusic.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.*
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _upNow = MutableStateFlow(SongCardState())
    private val _upNext = MutableStateFlow(SongCardState())
    private val _upLast = MutableStateFlow(SongCardState())
    private val _musicCardQueue = MusicCardStateQueue(_upNow, _upNext, _upLast)
    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _blockingErrorToast = MutableStateFlow<String?>(null)
    private val _jobQueue: Queue<MusicJob> = LinkedList()

    val upNow: StateFlow<SongCardState> = _upNow
    val upNext: StateFlow<SongCardState> = _upNext
    val upLast: StateFlow<SongCardState> = _upLast
    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _blockingErrorToast


    private fun jobRunner() {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                setIsLoading(true)
                var currentJob = _jobQueue.peek()
                currentJob?.let {
                    do {
                        val songObjectErrorWrapper = it.runJob()
                        if (songObjectErrorWrapper.blockingError != null) {
                            _blockingError.value = songObjectErrorWrapper.blockingError
                            _blockingErrorToast.value = _blockingError.value
                        } else {
                            _jobQueue.poll()
                            _musicCardQueue.push(songObjectErrorWrapper.songObject)
                            songObjectErrorWrapper.nonBlockingError?.let {
                                _nonBlockingError.value = it
                            }
                            _blockingError.value = null
                            currentJob = _jobQueue.peek()
                        }
                    } while (currentJob != null && blockingError.value == null)
                }
                setIsLoading(false)
            }
        }
    }

    init {
        retry()
    }

    private fun enqueueJob(musicJob: MusicJob) {
        _jobQueue.add(musicJob)
        jobRunner()
    }

    //add logic for empty state case
    fun likeSong(song: SongObject?, like: Boolean) {
        song?.let {
            _musicCardQueue.pop()
            enqueueJob(LikeSongJob(it, like, tokenStorage))
        }
    }

    fun cancelTop(){
        _musicCardQueue.pop()
        enqueueJob(GetSongJob(tokenStorage))
    }

    fun likeTop(like: Boolean) {
        if (upNow.value.songCardStateState.value != SongCardStateStates.Empty)
            upNow.value.songObject?.let { songObject -> likeSong(songObject, like) }
    }

    fun restartTop() {
        if(upNow.value.songCardStateState.value != SongCardStateStates.Empty)
            upNow.value.restart()
    }

    private fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun retry() {
        val requestFurther = MaxQueueSize - (_jobQueue.size + _musicCardQueue.size())
        for (i in 0 until requestFurther) {
            _jobQueue.add(GetSongJob(tokenStorage))
        }
        _nonBlockingError.value = null
        _blockingError.value = null
        jobRunner()
    }

    fun resetToastErrors() {
        _blockingErrorToast.value = null
    }

    fun resetState() {
        _jobQueue.clear()
        _musicCardQueue.clear()
        _nonBlockingError.value = null
        _blockingError.value = null
    }

    fun pauseCurrent() {
        upNow.value.pause()
    }

    fun resumeCurrent() {
        upNow.value.resume()
    }

    override fun onCleared() {
        super.onCleared()
        _musicCardQueue.release()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}


