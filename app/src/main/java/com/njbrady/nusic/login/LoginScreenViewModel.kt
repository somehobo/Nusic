package com.njbrady.nusic.login.model

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
    private val _userNameErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _passwordInput = MutableStateFlow("")
    private val _passwordErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _registerUserNameInput = MutableStateFlow("")
    private val _registerUsernameErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _registerPasswordInput = MutableStateFlow("")
    private val _registerPasswordErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _registerSecondaryPasswordInput = MutableStateFlow("")
    private val _registerEmailInput = MutableStateFlow("")
    private val _registerEmailErrorMessages = MutableStateFlow<List<String>>(emptyList())
    private val _errorMessages = MutableStateFlow<List<String>>(emptyList())

    val loginState: StateFlow<LoginStates> = _loginState
    val registerState: StateFlow<LoginStates> = _registerState
    val userNameInput: StateFlow<String> = _userNameInput
    val userNameErrorMessages: StateFlow<List<String>> = _userNameErrorMessages
    val passwordInput: StateFlow<String> = _passwordInput
    val passwordErrorMessages: StateFlow<List<String>> = _passwordErrorMessages
    val registerUserNameInput: StateFlow<String> = _registerUserNameInput
    val registerUserNameErrorMessages: StateFlow<List<String>> = _registerUsernameErrorMessages
    val registerPasswordInput: StateFlow<String> = _registerPasswordInput
    val registerPasswordErrorMessages: StateFlow<List<String>> = _registerPasswordErrorMessages
    val registerSecondaryPasswordInput: StateFlow<String> = _registerSecondaryPasswordInput
    val registerEmailInput: StateFlow<String> = _registerEmailInput
    val registerEmailErrorMessages: StateFlow<List<String>> = _registerEmailErrorMessages
    var errorMessage: StateFlow<List<String>> = _errorMessages

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

    fun resetGeneralErrorState() {
        _errorMessages.value = emptyList()
    }

    fun resetRegisterScreenState() {
        resetLoginState()
        setRegisterPassword("")
        setRegisterUserName("")
        setRegisterEmailInput("")
        setRegisterSecondaryPassword("")
        _errorMessages.value = emptyList()
        _registerEmailErrorMessages.value = emptyList()
        _registerPasswordErrorMessages.value = emptyList()
        _registerUsernameErrorMessages.value = emptyList()
    }

    fun resetLoginScreenState() {
        resetLoginState()
        setUserName("")
        setPassword("")
        _errorMessages.value = emptyList()
        _userNameErrorMessages.value = emptyList()
        _passwordErrorMessages.value = emptyList()
    }

    fun attemptLogin() {
        viewModelScope.launch {
            _loginState.value = LoginStates.Loading
            val loginRepository =
                loginRequest(userNameInput.value, passwordInput.value, tokenStorage)
            if (loginRepository.containsError) {
                _errorMessages.value = loginRepository.errorMessages
                _userNameErrorMessages.value = loginRepository.usernameError
                _passwordErrorMessages.value = loginRepository.passwordError
                _loginState.value = LoginStates.FillingOut
                return@launch
            }
            _loginState.value = LoginStates.Success
        }
    }

    fun attemptRegisterUser() {
        viewModelScope.launch {
            _loginState.value = LoginStates.Loading
            if (registerPasswordInput.value != registerSecondaryPasswordInput.value) {
                _loginState.value = LoginStates.FillingOut
                _errorMessages.value = listOf("Passwords do not match")
                return@launch
            }

            val registerRepository = registerRequest(
                registerUserNameInput.value,
                registerPasswordInput.value,
                registerEmailInput.value
            )

            if (registerRepository.containsError) {
                _registerUsernameErrorMessages.value = registerRepository.usernameError
                _registerPasswordErrorMessages.value = registerRepository.passwordError
                _registerEmailErrorMessages.value = registerRepository.emailError
                _errorMessages.value = registerRepository.errorMessages
                _loginState.value = LoginStates.FillingOut
                return@launch
            }

            val loginRepository =
                loginRequest(
                    _registerUserNameInput.value,
                    _registerPasswordInput.value,
                    tokenStorage
                )
            if (loginRepository.containsError) {
                _errorMessages.value = loginRepository.errorMessages
                _registerUsernameErrorMessages.value = loginRepository.usernameError
                _registerPasswordErrorMessages.value = loginRepository.passwordError
                _loginState.value = LoginStates.FillingOut
                return@launch
            }
            _loginState.value = LoginStates.Success
        }
    }

}

enum class LoginStates {
    Success, Error, Loading, FillingOut
}