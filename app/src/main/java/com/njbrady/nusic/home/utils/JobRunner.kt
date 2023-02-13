package com.njbrady.nusic.home.utils

import com.njbrady.nusic.home.HomeScreenViewModel
import com.njbrady.nusic.home.model.SongObjectErrorWrapper
import com.njbrady.nusic.utils.TokenStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class JobRunner(
    val scope: CoroutineScope,
    val onRecieved: (SongObjectErrorWrapper) -> Unit,
    private val defaultDispatcher: CoroutineDispatcher,
    private val tokenStorage: TokenStorage
) {

    private val _isLoading = MutableStateFlow(false)
    private val _nonBlockingError = MutableStateFlow<String?>(null)
    private val _blockingError = MutableStateFlow<String?>(null)
    private val _blockingErrorToast = MutableStateFlow<String?>(null)
    private val _jobQueue: Queue<MusicJob> = LinkedList()

    val isLoading: StateFlow<Boolean> = _isLoading
    val nonBlockingError: StateFlow<String?> = _nonBlockingError
    val blockingError: StateFlow<String?> = _blockingError
    val blockingErrorToast: StateFlow<String?> = _blockingErrorToast

    private fun jobRunner() {
        scope.launch {
            withContext(defaultDispatcher) {
                _isLoading.value = true
                var currentJob = _jobQueue.peek()
                currentJob?.let {
                    do {
                        val songObjectErrorWrapper = it.runJob()
                        if (songObjectErrorWrapper.blockingError != null) {
                            _blockingError.value = songObjectErrorWrapper.blockingError
                            _blockingErrorToast.value = _blockingError.value
                        } else {
                            _jobQueue.poll()
                            onRecieved(songObjectErrorWrapper)
                            songObjectErrorWrapper.nonBlockingError?.let {
                                _nonBlockingError.value = it
                            }
                            _blockingError.value = null
                            currentJob = _jobQueue.peek()
                        }
                    } while (currentJob != null && blockingError.value == null)
                }
                _isLoading.value = false
            }
        }
    }

    fun enqueueJob(musicJob: MusicJob) {
        _jobQueue.add(musicJob)
        jobRunner()
    }

    fun retry(musicCardQueueSize: Int) {
        val requestFurther = HomeScreenViewModel.MaxQueueSize - (_jobQueue.size + musicCardQueueSize)
        for (i in 0 until requestFurther) {
            _jobQueue.add(GetSongJob(tokenStorage))
        }
        _nonBlockingError.value = null
        _blockingError.value = null
        jobRunner()
    }

    fun release() {
        _jobQueue.clear()
    }

    fun resetToastErrors() {
        _blockingErrorToast.value = null
    }

    fun resetState() {
        _nonBlockingError.value = null
        _blockingError.value = null
    }
}