package com.njbrady.nusic.upload

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.njbrady.nusic.R
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.ExoPlayerPositionTracker
import com.njbrady.nusic.utils.calculateAmplitudes
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadScreenViewModel @Inject constructor(
    private val _localStorage: LocalStorage,
    @DefaultDispatcher private val _defaultDispatcher: CoroutineDispatcher,
    context: Context
) : ViewModel() {

    private val _songTitle = MutableStateFlow("")
    private val _songTitleErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _songPhotoUrl = MutableStateFlow<Uri?>(null)
    private val _songUrl = MutableStateFlow<Uri?>(null)
    private val _songAmplitude = MutableStateFlow(emptyList<Float>())
    private val _uploadSongLoading = MutableStateFlow(false)
    private val _uploadSongStartTime = MutableStateFlow(0)
    private val _uploadSongEndTime = MutableStateFlow(30)
    private val _uploadSongCurPos = MutableStateFlow<Int?>(null)
    private val _uploadSongPlayerState = MutableStateFlow(PlayerState.Playing)

    val username = _localStorage.retrieveUsername()
    val songTitle: StateFlow<String> = _songTitle
    val songTitleErrorMessages: StateFlow<List<String>> = _songTitleErrorMessages
    val songPhotoUrl: StateFlow<Uri?> = _songPhotoUrl
    val songUrl: StateFlow<Uri?> = _songUrl
    val songAmplitude: StateFlow<List<Float>> = _songAmplitude
    val uploadSongLoading: StateFlow<Boolean> = _uploadSongLoading
    val uploadSongStartTime: StateFlow<Int> = _uploadSongStartTime
    val uploadSongEndTime: StateFlow<Int> = _uploadSongEndTime
    val uploadSongPlayerState: StateFlow<PlayerState> = _uploadSongPlayerState
    val uploadSongCurPos: StateFlow<Int?> = _uploadSongCurPos
    var currentSong: ExoPlayer = ExoPlayer.Builder(context).build()
    var mediaPlayerTracker: ExoPlayerPositionTracker =
        ExoPlayerPositionTracker(currentSong, { position ->
            _uploadSongCurPos.value = position
        })

    init {
        currentSong.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY ->
                        if (_uploadSongPlayerState.value == PlayerState.Loading) _uploadSongPlayerState.value =
                            PlayerState.Paused else _uploadSongPlayerState.value =
                            PlayerState.Playing
                    Player.STATE_BUFFERING -> _uploadSongPlayerState.value = PlayerState.Loading
                    Player.STATE_ENDED -> _uploadSongPlayerState.value = PlayerState.Paused
                    else -> {}
                }
            }
        })
    }

    fun togglePlayState() {
        when (uploadSongPlayerState.value) {
            PlayerState.Playing -> {
                currentSong.pause()
                _uploadSongPlayerState.value = PlayerState.Paused
            }
            PlayerState.Paused -> {
                currentSong.seekTo((uploadSongStartTime.value * 1000).toLong())
                currentSong.play()
                _uploadSongPlayerState.value = PlayerState.Playing
            }
            else -> {}
        }
    }

    fun pauseWhenReady() {
        if (currentSong.isPlaying) {
            currentSong.pause()
            _uploadSongPlayerState.value = PlayerState.Paused
        }
    }

    fun setSongTitle(title: String) {
        _songTitle.value = title
    }

    fun setPhotoUrl(uri: Uri) {
        _songPhotoUrl.value = uri
    }

    fun setStartTime(int: Int) {
        _uploadSongStartTime.value = int
        _uploadSongEndTime.value = int + 30
    }

    fun setSongUrl(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _songUrl.value = uri
                _uploadSongLoading.value = true
                calculateAmplitudes(context = context, uri = uri) { amplitudes ->
                    Handler(Looper.getMainLooper()).post {
                        _uploadSongLoading.value = false
                        _uploadSongPlayerState.value = PlayerState.Loading
                        setStartTime(0)
                        _uploadSongCurPos.value = null
                        _songAmplitude.value = amplitudes
                        mediaPlayerTracker.stopTracking()
                        currentSong.removeMediaItem(0)
                        currentSong.setMediaItem(MediaItem.fromUri(uri))
                        currentSong.prepare()
                        mediaPlayerTracker.startTracking()
                    }
                }
            }
        }
    }

    fun clearPlayerState() {
        mediaPlayerTracker.stopTracking()
        currentSong.pause()
        currentSong.removeMediaItem(0)
    }

    fun clearState() {
        _songTitle.value = ""
        _songTitleErrorMessages.value = emptyList()
        _songPhotoUrl.value = null
        _songUrl.value = null
        setStartTime(0)
        _uploadSongCurPos.value = null
        clearPlayerState()
    }

    override fun onCleared() {
        super.onCleared()
        currentSong.release()
    }

    companion object {
        val DEFAULT_SONG = listOf<Float>(
            0.1F,
            0.9F,
            0.9F,
            0.3F,
            0.5F,
            0.8F,
            0.1F,
            0.1F,
            0.9F,
            0.9F,
            0.3F,
            0.5F,
            0.8F,
            0.1F,
            0.1F,
            0.9F,
            0.9F,
            0.3F,
            0.5F,
            0.8F,
            0.1F
        )
    }
}

enum class UploadScreenState {
    UploadSong, UploadPhoto, SongTitle, Preview
}

enum class UploadScreenAuxState {
    Passive, Loading
}

enum class PlayerState {
    Playing, Paused, Loading
}

fun UploadScreenState.stringResource(): Int {
    return when (this) {
        UploadScreenState.UploadPhoto -> R.string.upload_photo
        UploadScreenState.UploadSong -> R.string.upload_song
        UploadScreenState.SongTitle -> R.string.song_title
        UploadScreenState.Preview -> R.string.song_card_preview
    }
}