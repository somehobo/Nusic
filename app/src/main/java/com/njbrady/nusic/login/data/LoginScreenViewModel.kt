package com.njbrady.nusic.login.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.login.requests.loginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginStates.FillingOut)
    private val _userNameInput = MutableStateFlow("")
    private val _passwordInput = MutableStateFlow("")

    val loginState: StateFlow<LoginStates> = _loginState
    var userNameInput: StateFlow<String> = _userNameInput
    var passwordInput: StateFlow<String> = _passwordInput
    var errorMessage: String? = null

    fun setUserName(input: String) {
        _userNameInput.value = input
    }

    fun setPassword(input: String) {
        _passwordInput.value = input
    }

    fun resetLoginState() {
        _loginState.value = LoginStates.FillingOut
    }

    fun resetState() {
        resetLoginState()
        setUserName("")
        setPassword("")
    }

    fun attemptLogin() {
        viewModelScope.launch {
            _loginState.value = LoginStates.Loading
            errorMessage = loginRequest(_userNameInput.value, _passwordInput.value, tokenStorage)
            if (errorMessage == "") {
                _loginState.value = LoginStates.Success
            } else {
                _loginState.value = LoginStates.Error
            }
        }
    }

}

enum class LoginStates {
    Success, Error, Loading, FillingOut
}