package com.njbrady.nusic.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.njbrady.nusic.R
import com.njbrady.nusic.utils.LocalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UploadScreenViewModel @Inject constructor(
    private val localStorage: LocalStorage
) : ViewModel() {

    private val _songTitle = MutableStateFlow("")
    private val _songTitleErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _songPhotoUrl = MutableStateFlow<Uri?>(null)
    private val _songUrl = MutableStateFlow<Uri?>(null)
    private val _uploadScreenState = MutableStateFlow(UploadScreenState.UploadPhoto)
    private val _uploadScreenAuxState = MutableStateFlow(UploadScreenAuxState.Passive)

    val username = localStorage.retrieveUsername()
    val songTitle: StateFlow<String> = _songTitle
    val songTitleErrorMessages: StateFlow<List<String>> = _songTitleErrorMessages
    val songPhotoUrl: StateFlow<Uri?> = _songPhotoUrl
    val songUrl: StateFlow<Uri?> = _songUrl
    val uploadScreenState: StateFlow<UploadScreenState> = _uploadScreenState
    val uploadScreenAuxState: StateFlow<UploadScreenAuxState> = _uploadScreenAuxState


    fun setSongTitle(title: String) {
        _songTitle.value = title
    }

    fun setPhotoUrl(uri: Uri) {
        _songPhotoUrl.value = uri
    }

    fun setSongUrl(uri: Uri) {
        _songUrl.value = uri

        //Start FFT here i believe
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