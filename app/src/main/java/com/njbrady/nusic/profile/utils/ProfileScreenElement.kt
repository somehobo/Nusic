package com.njbrady.nusic.profile.utils

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ProfilePhoto() {

    private val _photoUrl = MutableStateFlow<String?>(null)
    private val _profilePhotoState = MutableStateFlow(ProfilePhotoState.Loading)

    val photoUrl: StateFlow<String?> = _photoUrl
    val profilePhotoState: StateFlow<ProfilePhotoState> = _profilePhotoState


    init {
        //get profile photo or first initial
        _profilePhotoState.value = ProfilePhotoState.Loading
        _photoUrl.value = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=735&q=80"
        _profilePhotoState.value = ProfilePhotoState.Success
    }

    fun setImage(Uri: Uri) {
        _profilePhotoState.value = ProfilePhotoState.SuccessPending
        _photoUrl.value = Uri.toString()
    }

}

enum class ProfilePhotoState {
    Loading, Error, Success, SuccessPending
}