package com.njbrady.nusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.profile.utils.ProfileGridDataSource
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val tokenStorage: TokenStorage,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    val likedSongs: Flow<PagingData<SongObject>> = Pager(config = PagingConfig(pageSize = 20), pagingSourceFactory  =  {
        ProfileGridDataSource(tokenStorage)
    }).flow.cachedIn(viewModelScope)

    private var onLogoutHit: () -> Unit = {}

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

}
