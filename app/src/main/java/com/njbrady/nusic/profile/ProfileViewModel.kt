package com.njbrady.nusic.profile

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.njbrady.nusic.MainSocketHandler
import com.njbrady.nusic.login.model.GeneralStates
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.profile.requests.getUserAttributes
import com.njbrady.nusic.profile.requests.uploadBio
import com.njbrady.nusic.profile.utils.ProfileMessageHandler
import com.njbrady.nusic.profile.utils.ProfilePagedDataSource
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.upload.PlayerState
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.UserModel
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileViewModel @AssistedInject constructor(
    private val localStorage: LocalStorage,
    @DefaultDispatcher private val _defaultDispatcher: CoroutineDispatcher,
    private val _mainSocketHandler: MainSocketHandler,
    private val _exoPlayerMiddleMan: ExoMiddleMan,
    @Assisted _otherUserModel: UserModel? = null
) : ViewModel() {

    private var _onLogoutHit: () -> Unit = {}
    private val _refreshingProfile = MutableStateFlow(false)
    private val _prependedLikedSongs = MutableStateFlow(listOf<Pair<SongPlayerWrapper, Boolean>>())
    private val _prependedCreatedSongs =
        MutableStateFlow(listOf<Pair<SongPlayerWrapper, Boolean>>())
    private val _bioState = MutableStateFlow(GeneralStates.Loading)
    private val _bio = MutableStateFlow<String?>(null)
    private val _bioErrors = MutableStateFlow<List<String>?>(null)
    private val userModel = _otherUserModel ?: localStorage.retrieveUserModel()

    val refreshingProfile: StateFlow<Boolean> = _refreshingProfile
    val prependedLikedSongs: StateFlow<List<Pair<SongPlayerWrapper, Boolean>>> =
        _prependedLikedSongs
    val prependedCreatedSongs: StateFlow<List<Pair<SongPlayerWrapper, Boolean>>> =
        _prependedCreatedSongs
    val psd = _exoPlayerMiddleMan.psd
    val topSongState: StateFlow<PlayerState> = _exoPlayerMiddleMan.currentSongPlayerState
    val topSongErrorMessage: StateFlow<String?> = _exoPlayerMiddleMan.currentSongErrorMessage
    val bio: StateFlow<String?> = _bio
    val bioState: StateFlow<GeneralStates> = _bioState
    val bioErrors: StateFlow<List<String>?> = _bioErrors
    val userName = userModel.userName
    var currentlyPlayingSong: SongPlayerWrapper? = null
    var selectedSongIndex = 0
    var uploadedBio: String? = null


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

    init {
        getAndUpdateUserAttributes()
    }

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
        getAndUpdateUserAttributes()
    }

    fun updateTempBio(new: String) {
        _bio.value = new
    }

    fun resetBio() {
        _bio.value = uploadedBio
    }

    fun onFocusing() {
        _bioState.value = GeneralStates.FillingOut
    }

    fun uploadCurrentBio() {
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _bio.value?.let {
                    _bioState.value = GeneralStates.Loading
                    val errorModel = uploadBio(localStorage, it)
                    if(errorModel == null) {
                        _bioState.value = GeneralStates.Success
                        _bioErrors.value = null
                    } else {
                        _bioState.value = GeneralStates.Error
                        _bioErrors.value = errorModel.bioErrors
                    }
                }
            }
        }
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

    private fun getAndUpdateUserAttributes() {
        viewModelScope.launch {
            withContext(_defaultDispatcher) {
                _bioState.value = GeneralStates.Loading
                try {
                    val attributes = getUserAttributes(localStorage,userModel)
                    _bio.value = attributes.bio
                    profilePhoto.setInitialImage(attributes.profilePhotoUrl)
                    uploadedBio = attributes.bio
                    _bioState.value = GeneralStates.Success
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }


    companion object {
        // This must match the backend constant as well
        const val PAGE_SIZE = 6

        fun provideProfileViewModelFactory(factory: Factory, userModel: UserModel?): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(userModel) as T
                }
            }
        }

    }

    @AssistedFactory
    interface Factory {
        fun create(userModel: UserModel?): ProfileViewModel
    }

}
