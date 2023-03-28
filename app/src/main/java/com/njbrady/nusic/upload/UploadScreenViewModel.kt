package com.njbrady.nusic.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.R
import com.njbrady.nusic.utils.LocalStorage
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
) : ViewModel() {

    private val _songTitle = MutableStateFlow("")
    private val _songTitleErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _songPhotoUrl = MutableStateFlow<Uri?>(null)
    private val _songUrl = MutableStateFlow<Uri?>(null)
    private val _songAmplitude = MutableStateFlow(emptyList<Float>())
    private val _uploadScreenState = MutableStateFlow(UploadScreenState.UploadPhoto)
    private val _uploadScreenAuxState = MutableStateFlow(UploadScreenAuxState.Passive)
    private val _uploadSongLoading = MutableStateFlow(false)

    val username = _localStorage.retrieveUsername()
    val songTitle: StateFlow<String> = _songTitle
    val songTitleErrorMessages: StateFlow<List<String>> = _songTitleErrorMessages
    val songPhotoUrl: StateFlow<Uri?> = _songPhotoUrl
    val songUrl: StateFlow<Uri?> = _songUrl
    val songAmplitude: StateFlow<List<Float>> = _songAmplitude
    val uploadScreenState: StateFlow<UploadScreenState> = _uploadScreenState
    val uploadScreenAuxState: StateFlow<UploadScreenAuxState> = _uploadScreenAuxState
    val uploadSongLoading: StateFlow<Boolean> = _uploadSongLoading


    fun setSongTitle(title: String) {
        _songTitle.value = title
    }

    fun setPhotoUrl(uri: Uri) {
        _songPhotoUrl.value = uri
    }

    fun setSongUrl(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _songUrl.value = uri
                _uploadSongLoading.value = true
                calculateAmplitudes(context = context, uri = uri) { amplitudes ->
                    _uploadSongLoading.value = false
                    _songAmplitude.value = amplitudes
                }
            }
        }
    }

    fun clearState() {
        _songTitle.value = ""
        _songTitleErrorMessages.value = emptyList()
        _songPhotoUrl.value = null
        _songUrl.value = null
        _uploadScreenState.value = UploadScreenState.UploadPhoto
        _uploadScreenAuxState.value = UploadScreenAuxState.Passive
    }

    companion object {
        val DEFAULT_SONG = listOf<Float>(0.1F, 0.9F, 0.9F, 0.3F, 0.5F, 0.8F, 0.1F,0.1F, 0.9F, 0.9F, 0.3F, 0.5F, 0.8F, 0.1F,0.1F, 0.9F, 0.9F, 0.3F, 0.5F, 0.8F, 0.1F)
    }
}

enum class UploadScreenState {
    UploadSong, UploadPhoto, SongTitle, Preview
}

enum class UploadScreenAuxState {
    Passive, Loading
}

fun UploadScreenState.stringResource(): Int {
    return when (this) {
        UploadScreenState.UploadPhoto -> R.string.upload_photo
        UploadScreenState.UploadSong -> R.string.upload_song
        UploadScreenState.SongTitle -> R.string.song_title
        UploadScreenState.Preview -> R.string.song_card_preview
    }
}