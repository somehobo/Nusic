package com.njbrady.nusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.home.data.HomeState
import com.njbrady.nusic.home.data.Song
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {
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

    fun likeSong(song: Song, liked: Boolean) {
        //do nothing with liked for now
        homeState.songList.removeLast()
        print("liked song, ${homeState.songList.size} left")
        homeState.songList.add(0,MockData.songList.random())
    }
}
