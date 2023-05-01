package com.njbrady.nusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.profile.utils.ProfileMessageHandler
import com.njbrady.nusic.profile.utils.ProfilePagedDataSource
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.upload.PlayerState
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val localStorage: LocalStorage,
    @DefaultDispatcher private val _defaultDispatcher: CoroutineDispatcher,
    private val _mainSocketHandler: MainSocketHandler,
    private val _exoPlayerMiddleMan: ExoMiddleMan
) : ViewModel() {

    private var _onLogoutHit: () -> Unit = {}
    private val _refreshingProfile = MutableStateFlow(false)
    private val _prependedLikedSongs = MutableStateFlow(listOf<Pair<SongPlayerWrapper, Boolean>>())
    private val _prependedCreatedSongs =
        MutableStateFlow(listOf<Pair<SongPlayerWrapper, Boolean>>())
    private val userModel = localStorage.retrieveUserModel()

    val refreshingProfile: StateFlow<Boolean> = _refreshingProfile
    val prependedLikedSongs: StateFlow<List<Pair<SongPlayerWrapper, Boolean>>> =
        _prependedLikedSongs
    val prependedCreatedSongs: StateFlow<List<Pair<SongPlayerWrapper, Boolean>>> =
        _prependedCreatedSongs
    val psd = _exoPlayerMiddleMan.psd
    val topSongState: StateFlow<PlayerState> = _exoPlayerMiddleMan.currentSongPlayerState
    val topSongErrorMessage: StateFlow<String?> = _exoPlayerMiddleMan.currentSongErrorMessage
    val userName = userModel.userName
    var currentlyPlayingSong: SongPlayerWrapper? = null
    var selectedSongIndex = 0


    private val messageHandler =
        ProfileMessageHandler(onSongReceived = { songPlayerWrapper, type, liked ->
            when (type) {
                SongListType.Liked -> {
                    val likedSongsList = _prependedLikedSongs.value.filter {
                        it.first.songModel.songId != songPlayerWrapper.songModel.songId
                    }
                    _prependedLikedSongs.value =
                        listOf(Pair(songPlayerWrapper, liked)) + likedSongsList
                }
                SongListType.Created -> {
                    val createdSongsList = _prependedCreatedSongs.value.filter {
                        it.first.songModel.songId != songPlayerWrapper.songModel.songId
                    }
                    _prependedCreatedSongs.value =
                        listOf(Pair(songPlayerWrapper, liked)) + createdSongsList
                }
            }
        }, onError = {}, onBlockingError = {}, mainSocketHandler = _mainSocketHandler,
            exoMiddleMan = _exoPlayerMiddleMan
        )

    val likedSongs: Flow<PagingData<SongPlayerWrapper>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            ProfilePagedDataSource(localStorage, SongListType.Liked, _exoPlayerMiddleMan)
        }).flow.cachedIn(viewModelScope)

    val createdSongs: Flow<PagingData<SongPlayerWrapper>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            ProfilePagedDataSource(localStorage, SongListType.Created, _exoPlayerMiddleMan)
        }).flow.cachedIn(viewModelScope)

    val profilePhoto = ProfilePhoto(
        scope = viewModelScope, localStorage = localStorage, defaultDispatcher = _defaultDispatcher
    )

    fun setCurrentPlayingScrollingSong(songPlayerWrapper: SongPlayerWrapper, index: Int) {
        currentlyPlayingSong = songPlayerWrapper
        selectedSongIndex = index
    }

    fun logout() {
        _onLogoutHit()
        _mainSocketHandler.disconnect()
        localStorage.deleteUserModel()
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
        _onLogoutHit = function
    }

    fun getOnLogoutHit(): () -> Unit {
        return _onLogoutHit
    }

    fun pauseAndReset() {
        _exoPlayerMiddleMan.pauseCurrent()
        currentlyPlayingSong = null
    }

    fun pauseCurrent() {
        _exoPlayerMiddleMan.pauseCurrent()
    }

    fun resumeCurrent() {
        currentlyPlayingSong?.play?.let { it() }
    }


    companion object {
        // This must match the backend constant as well
        const val PAGE_SIZE = 6

    }

}
