package com.njbrady.nusic

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.njbrady.nusic.home.utils.SongCardState
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.profile.utils.ProfileMessageHandler
import com.njbrady.nusic.profile.utils.ProfilePagedDataSource
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val localStorage: LocalStorage,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    val mainSocketHandler: MainSocketHandler
) : ViewModel() {

    private var onLogoutHit: () -> Unit = {}
    private val _refreshingProfile = MutableStateFlow(false)
    private val _prependedLikedSongs = MutableStateFlow(listOf<Pair<SongCardState, Boolean>>())
    private val _prependedCreatedSongs = MutableStateFlow(listOf<Pair<SongCardState, Boolean>>())

    val refreshingProfile: StateFlow<Boolean> = _refreshingProfile
    val prependedLikedSongs: StateFlow<List<Pair<SongCardState, Boolean>>> = _prependedLikedSongs
    val prependedCreatedSongs: StateFlow<List<Pair<SongCardState, Boolean>>> = _prependedCreatedSongs
    val username = localStorage.retrieveUsername()
    var currentlyPlayingSong: SongCardState? = null


    private val messageHandler = ProfileMessageHandler(onSongReceived = { songCardState, type, liked ->
        when (type) {
            Type.Liked -> {
                val likedSongsList = _prependedLikedSongs.value.filter {
                    it.first.songObject?.songId != songCardState.songObject?.songId
                }
                _prependedLikedSongs.value =
                    listOf(Pair(songCardState, liked)) + likedSongsList
            }
            Type.Created -> {
                val createdSongsList = _prependedCreatedSongs.value.filter {
                    it.first.songObject?.songId != songCardState.songObject?.songId
                }
                _prependedCreatedSongs.value =
                    listOf(Pair(songCardState, liked)) + createdSongsList
            }
        }
    }, onError = {}, onBlockingError = {}, mainSocketHandler = mainSocketHandler
    )

    val likedSongs: Flow<PagingData<SongCardState>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            ProfilePagedDataSource(localStorage, Type.Liked)
        }).flow.cachedIn(viewModelScope)

    val createdSongs: Flow<PagingData<SongCardState>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            ProfilePagedDataSource(localStorage, Type.Created)
        }).flow.cachedIn(viewModelScope)

    val profilePhoto = ProfilePhoto(
        scope = viewModelScope, localStorage = localStorage, defaultDispatcher = defaultDispatcher
    )

    fun logout() {
        onLogoutHit()
        mainSocketHandler.disconnect()
        localStorage.deleteUsername()
        localStorage.deleteToken()
    }

    fun setRefresh(state: Boolean) {
        _refreshingProfile.value = state
    }

    fun refreshProfile() {
        _prependedLikedSongs.value = emptyList()
        _prependedCreatedSongs.value = emptyList()
        profilePhoto.refresh()
    }

    fun setOnLogoutHit(function: () -> Unit) {
        onLogoutHit = function
    }

    fun getOnLogoutHit(): () -> Unit {
        return onLogoutHit
    }

    fun pauseAndReset() {
        currentlyPlayingSong?.pauseWhenReady()
        currentlyPlayingSong = null
    }

    fun pauseCurrent() {
        currentlyPlayingSong?.pauseWhenReady()
    }

    fun resumeCurrent() {
        currentlyPlayingSong?.resumePreviousPlayState()
    }

    fun uploadProfilePicture(uri: Uri, context: Context) {
        profilePhoto.setImage(uri = uri, context = context)
    }

    private fun onMessageRecieved(jsonObject: JSONObject) {
        val messageType = jsonObject.get("type")
    }

    companion object {
        // This must match the backend constant as well
        const val PAGE_SIZE = 6

    }

}
