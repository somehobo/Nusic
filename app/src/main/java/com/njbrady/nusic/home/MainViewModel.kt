package com.njbrady.nusic.home

import androidx.lifecycle.ViewModel
import com.njbrady.nusic.utils.MockData
import com.njbrady.nusic.home.model.HomeState
import com.njbrady.nusic.utils.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val tokenStorage: TokenStorage): ViewModel() {
    val homeState = HomeState()
    init {
        getInitialSongs(2)
    }

    fun getInitialSongs(count: Int) {
        //temporary
        print("get initial")
        for (i in 0 until count) {
            val random = (0 until MockData.songList.size-1).random()
            homeState.songList.add(MockData.songList[random])
        }
    }

    fun likeSong(liked: Boolean) {
        //do nothing with liked for now
        homeState.songList.removeLast()
        print("liked song, ${homeState.songList.size} left")
        homeState.songList.add(0, MockData.songList.random())
    }

    fun logout() {
        tokenStorage.deleteToken()
    }
}
