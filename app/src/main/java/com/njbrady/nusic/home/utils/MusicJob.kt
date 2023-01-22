package com.njbrady.nusic.home.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.njbrady.nusic.home.requests.getSong
import com.njbrady.nusic.home.requests.likeSong
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.responseObjects.SongObjectErrorWrapper
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class MusicJob(val tokenStorage: TokenStorage) {

    // If network error or other this should throw an error
    abstract fun runJob(): SongObjectErrorWrapper
}


class GetSongJob(tokenStorage: TokenStorage) : MusicJob(tokenStorage) {
    override fun runJob(): SongObjectErrorWrapper {
        return getSong(tokenStorage)
    }
}

class LikeSongJob(
    private val songObject: SongObject,
    private val liked: Boolean,
    tokenStorage: TokenStorage
) : MusicJob(tokenStorage) {
    override fun runJob(): SongObjectErrorWrapper {
        return likeSong(songObject, liked, tokenStorage)
    }
}
