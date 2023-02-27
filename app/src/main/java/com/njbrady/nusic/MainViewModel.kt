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
import com.njbrady.nusic.profile.utils.ProfilePagedDataSource
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.utils.OnSocketRoute
import com.njbrady.nusic.utils.TokenStorage
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
    val tokenStorage: TokenStorage,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    val mainSocketHandler: MainSocketHandler
) : ViewModel() {

    init {
        mainSocketHandler.subscribeNewRoute(
            route = OnSocketRoute.HOMEROUTE
        ) { jsonObject -> onMessageRecieved(jsonObject = jsonObject) }
    }

    private val _refreshingProfile = MutableStateFlow(false)
    private var onLogoutHit: () -> Unit = {}

    var currentlyPlayingSong: SongCardState? = null

    val likedSongs: Flow<PagingData<SongCardState>> = Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory  =  {
        ProfilePagedDataSource(tokenStorage, Type.Liked)
    }).flow.cachedIn(viewModelScope)


    val createdSongs: Flow<PagingData<SongCardState>> = Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory  =  {
        ProfilePagedDataSource(tokenStorage, Type.Created)
    }).flow.cachedIn(viewModelScope)

    val profilePhoto = ProfilePhoto(scope = viewModelScope, tokenStorage = tokenStorage, defaultDispatcher = defaultDispatcher)

    val refreshingProfile: StateFlow<Boolean> = _refreshingProfile

    fun refreshProfile() {
        _refreshingProfile.value = true
    }

    fun logout() {
        onLogoutHit()
        tokenStorage.deleteToken()
    }

    fun setOnLogoutHit(function :() -> Unit) {
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
