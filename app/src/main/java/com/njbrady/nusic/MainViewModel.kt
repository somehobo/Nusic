package com.njbrady.nusic

import androidx.lifecycle.ViewModel
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val tokenStorage: TokenStorage,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

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
