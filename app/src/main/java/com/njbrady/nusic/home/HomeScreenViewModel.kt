package com.njbrady.nusic.home

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.GetSongJob
import com.njbrady.nusic.home.utils.LikeSongJob
import com.njbrady.nusic.home.utils.MusicJob
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

/*
known bugs:
- Sometimes a duplicate music card appears
- reload button is not visible
 */

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _audioPlayer = MediaPlayer()
    private val _upNow = MutableStateFlow<SongObject?>(null)
    private val _upNext = MutableStateFlow<SongObject?>(null)
    private val _upLast = MutableStateFlow<SongObject?>(null)
    private val _musicCardQueue = MusicCardQueue(_upNow, _upNext, _upLast)
    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _blockingErrorToast = MutableStateFlow<String?>(null)
    private val _jobQueue: Queue<MusicJob> = LinkedList()

    val upNow: StateFlow<SongObject?> = _upNow
    val upNext: StateFlow<SongObject?> = _upNext
    val upLast: StateFlow<SongObject?> = _upLast
    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _blockingErrorToast


    private fun jobRunner() {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                setIsLoading(true)
                var currentJob = _jobQueue.peek()
                while (currentJob != null && blockingError.value == null) {
                    val songObjectErrorWrapper = currentJob.runJob()
                    if (songObjectErrorWrapper.blockingError != null) {
                        _blockingError.value = songObjectErrorWrapper.blockingError
                        _blockingErrorToast.value = _blockingError.value
                    } else {
                        _jobQueue.poll()
                        _musicCardQueue.push(songObjectErrorWrapper.songObject)
                        songObjectErrorWrapper.nonBlockingError?.let {
                            _nonBlockingError.value = it
                        }
                        currentJob = _jobQueue.peek()
                    }
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


    fun likeSong(song: SongObject, like: Boolean) {
        _musicCardQueue.pop()
        enqueueJob(LikeSongJob(song, like, tokenStorage))
    }

    fun likeTop(like: Boolean) {
        upNow.value?.let {
            likeSong(it, like)
        }
    }

    private fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun retry() {
        val requestFurther = MaxQueueSize - _jobQueue.size + _musicCardQueue.size()
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

    override fun onCleared() {
        super.onCleared()
        _audioPlayer.release()
    }

    companion object {
        const val MaxQueueSize = 3
    }
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

