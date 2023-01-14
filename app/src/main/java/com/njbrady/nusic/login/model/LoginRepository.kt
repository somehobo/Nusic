package com.njbrady.nusic.login.model

data class LoginRepository(
    val usernameError: List<String> = emptyList(),
    val emailError: List<String> = emptyList(),
    val passwordError: List<String> = emptyList(),
    val errorMessages: List<String> = emptyList(),
    val containsError: Boolean = false
)