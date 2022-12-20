package com.njbrady.nusic.home.data
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeState {
    val songList = mutableStateListOf<Song>()
}