package com.njbrady.nusic.login.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.login.requests.loginRequest
import com.njbrady.nusic.login.requests.registerRequest
import com.njbrady.nusic.utils.TokenStorage
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
    private val _registerState = MutableStateFlow(LoginStates.FillingOut)
    private val _userNameInput = MutableStateFlow("")
    private val _passwordInput = MutableStateFlow("")
    private val _registerUserNameInput = MutableStateFlow("")
    private val _registerPasswordInput = MutableStateFlow("")
    private val _registerSecondaryPasswordInput = MutableStateFlow("")
    private val _registerEmailInput = MutableStateFlow("")

    val loginState: StateFlow<LoginStates> = _loginState
    val registerState: StateFlow<LoginStates> = _registerState
    val userNameInput: StateFlow<String> = _userNameInput
    val passwordInput: StateFlow<String> = _passwordInput
    val registerUserNameInput: StateFlow<String> = _registerUserNameInput
    val registerPasswordInput: StateFlow<String> = _registerPasswordInput
    val registerSecondaryPasswordInput: StateFlow<String> = _registerSecondaryPasswordInput
    val registerEmailInput: StateFlow<String> = _registerEmailInput
    var errorMessage: String? = null

    fun setUserName(input: String) {
        _userNameInput.value = input
    }

    fun setPassword(input: String) {
        _passwordInput.value = input
    }

    fun setRegisterUserName(input: String) {
        _registerUserNameInput.value = input
    }

    fun setRegisterPassword(input: String) {
        _registerPasswordInput.value = input
    }

    fun setRegisterSecondaryPassword(input: String) {
        _registerSecondaryPasswordInput.value = input
    }

    fun setRegisterEmailInput(input: String) {
        _registerEmailInput.value = input
    }

    fun resetLoginState() {
        _loginState.value = LoginStates.FillingOut
    }

    fun resetRegisterScreenState() {
        resetLoginState()
        setRegisterPassword("")
        setRegisterUserName("")
        setRegisterEmailInput("")
        setRegisterSecondaryPassword("")
    }

    fun resetLoginScreenState() {
        resetLoginState()
        setUserName("")
        setPassword("")
    }

    fun attemptLogin() {
        viewModelScope.launch {
            _loginState.value = LoginStates.Loading
            errorMessage = loginRequest(userNameInput.value, passwordInput.value, tokenStorage)
            if (errorMessage == "") {
                _loginState.value = LoginStates.Success
            } else {
                _loginState.value = LoginStates.Error
            }
        }
    }

    fun attemptRegisterUser() {
        viewModelScope.launch {
            _loginState.value = LoginStates.Loading
            if (registerPasswordInput.value != registerSecondaryPasswordInput.value) {
                _loginState.value = LoginStates.Error
                errorMessage = "Passwords do not match"
                return@launch
            }

            val message = registerRequest(
                registerUserNameInput.value,
                registerPasswordInput.value,
                registerEmailInput.value
            )

            if (message != "") {
                _loginState.value = LoginStates.Error
                errorMessage = message
                return@launch
            }

            _userNameInput.value = registerUserNameInput.value
            _passwordInput.value = registerPasswordInput.value

            attemptLogin()
        }
    }

}

enum class LoginStates {
    Success, Error, Loading, FillingOut
}