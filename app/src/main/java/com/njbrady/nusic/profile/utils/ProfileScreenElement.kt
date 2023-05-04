package com.njbrady.nusic.profile.utils

import android.content.Context
import android.net.Uri
import com.njbrady.nusic.profile.requests.uploadProfilePhoto
import com.njbrady.nusic.utils.LocalStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfilePhoto(
    private val scope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher,
    private val localStorage: LocalStorage,
) {

    private val _photoUrl = MutableStateFlow<String?>(null)
    private val _profilePhotoState = MutableStateFlow(ProfilePhotoState.Loading)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val photoUrl: StateFlow<String?> = _photoUrl
    val profilePhotoState: StateFlow<ProfilePhotoState> = _profilePhotoState
    val errorMessage: StateFlow<String?> = _errorMessage


    fun setInitialImage(url: String) {
        _photoUrl.value = url
        _profilePhotoState.value = ProfilePhotoState.Success
    }

    fun setImage(uri: Uri, context: Context) {
        scope.launch {
            _profilePhotoState.value = ProfilePhotoState.SuccessPending
            _photoUrl.value = uri.toString()
            val oldPhoto = _photoUrl.value
            try {
                uploadProfilePhoto(localStorage = localStorage, uri = uri, context = context)
                _profilePhotoState.value = ProfilePhotoState.Success
            } catch (error: Exception) {
                _errorMessage.value = error.message
                _profilePhotoState.value = ProfilePhotoState.Error
                _photoUrl.value = oldPhoto
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

}

enum class ProfilePhotoState {
    Loading, Error, Success, SuccessPending
}