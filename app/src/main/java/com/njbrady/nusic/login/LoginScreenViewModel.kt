package com.njbrady.nusic.login.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.njbrady.nusic.login.requests.loginRequest
import com.njbrady.nusic.login.requests.registerRequest
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    val localStorage: LocalStorage,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher

) : ViewModel() {
    private val _loginState = MutableStateFlow(GeneralStates.FillingOut)
    private val _registerState = MutableStateFlow(GeneralStates.FillingOut)
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

    val loginState: StateFlow<GeneralStates> = _loginState
    val registerState: StateFlow<GeneralStates> = _registerState
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
        _loginState.value = GeneralStates.FillingOut
    }

    fun resetRegisterState() {
        _registerState.value = GeneralStates.FillingOut
    }

    fun resetGeneralErrorState() {
        _errorMessages.value = emptyList()
    }

    fun resetRegisterScreenState() {
        resetRegisterState()
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
        _loginState.value = GeneralStates.FillingOut
    }

    fun attemptLogin() {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                _loginState.value = GeneralStates.Loading
                val loginRepository =
                    loginRequest(userNameInput.value, passwordInput.value, localStorage)
                if (loginRepository.containsError) {
                    _errorMessages.value = loginRepository.errorMessages
                    _userNameErrorMessages.value = loginRepository.usernameError
                    _passwordErrorMessages.value = loginRepository.passwordError
                    _loginState.value = GeneralStates.FillingOut
                } else {
                    _loginState.value = GeneralStates.Success
                }
            }
        }
    }

    fun attemptRegisterUser() {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                _registerState.value = GeneralStates.Loading
                if (registerPasswordInput.value != registerSecondaryPasswordInput.value) {
                    _registerState.value = GeneralStates.FillingOut
                    _errorMessages.value = listOf("Passwords don't match")
                } else {
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
                        _registerState.value = GeneralStates.FillingOut
                    } else {
                        val loginRepository =
                            loginRequest(
                                _registerUserNameInput.value,
                                _registerPasswordInput.value,
                                localStorage
                            )
                        if (loginRepository.containsError) {
                            _errorMessages.value = loginRepository.errorMessages
                            _registerUsernameErrorMessages.value = loginRepository.usernameError
                            _registerPasswordErrorMessages.value = loginRepository.passwordError
                            _registerState.value = GeneralStates.FillingOut
                        } else {
                            _registerState.value = GeneralStates.Success
                        }
                    }
                }
            }
        }
    }

}

enum class GeneralStates {
    Success, Error, Loading, FillingOut
}