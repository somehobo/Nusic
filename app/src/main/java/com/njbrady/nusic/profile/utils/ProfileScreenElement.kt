package com.njbrady.nusic.profile.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State


class ProfilePhoto() {

    private val _photoUrl = MutableStateFlow<String?>(null)
    private val _profilePhotoState = MutableStateFlow(ProfilePhotoState.Loading)

    val photoUrl: StateFlow<String?> = _photoUrl
    val profilePhotoState: StateFlow<ProfilePhotoState> = _profilePhotoState

    init {
        //get profile photo or first initial
        _profilePhotoState.value = ProfilePhotoState.Loading
        _photoUrl.value = "https://www.google.com/url?sa=i&url=https%3A%2F%2Funsplash.com%2Fs%2Fphotos%2Ffunny-cat&psig=AOvVaw1gEE-Jjfs3axGIVm6CWQ_f&ust=1675574779343000&source=images&cd=vfe&ved=0CA8QjRxqFwoTCODY6s6Q-_wCFQAAAAAdAAAAABAD"
        _profilePhotoState.value = ProfilePhotoState.Success
    }

}

enum class ProfilePhotoState {
    Loading, Error, Success
}