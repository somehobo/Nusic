package com.njbrady.nusic.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.login.data.LoginScreenViewModel
import com.njbrady.nusic.login.data.LoginStates

@Composable
fun LoginScreen(loginScreenViewModel: LoginScreenViewModel) {
    LoginContent(loginScreenViewModel = loginScreenViewModel)
}

@Composable
private fun LoginContent(
    loginScreenViewModel: LoginScreenViewModel
) {
    val username by loginScreenViewModel.userNameInput.collectAsState()
    val password by loginScreenViewModel.passwordInput.collectAsState()
    val loginState by loginScreenViewModel.loginState.collectAsState()
    Scaffold { paddingValues ->
        if (loginState == LoginStates.Loading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues.calculateBottomPadding())
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val loginFieldModifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(8.dp)
                LoginField(
                    hint = "Username",
                    value = username,
                    isPassword = false,
                    onValueChange = { input -> loginScreenViewModel.setUserName(input) },
                    modifier = loginFieldModifier
                )
                LoginField(
                    hint = "Password",
                    value = password,
                    isPassword = true,
                    onValueChange = { input -> loginScreenViewModel.setPassword(input) },
                    modifier = loginFieldModifier
                )
                Button(onClick = { loginScreenViewModel.attemptLogin() }) {
                    Text(text = "Login")
                }
            }
            if (loginState == LoginStates.Error) {
                Toast.makeText(
                    LocalContext.current,
                    loginScreenViewModel.errorMessage,
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetLoginState()
            } else if (loginState == LoginStates.Success) {
                Toast.makeText(
                    LocalContext.current,
                    "LOGGED IN WOOOOO",
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetState()
            }
        }
    }

}

@Composable
fun LoginField(
    hint: String,
    value: String,
    isPassword: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val showPassword = remember { mutableStateOf(isPassword.not()) }

    TextField(
        modifier = modifier,
        value = value,
        placeholder = { Text(text = hint) },
        visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        onValueChange = { onValueChange(it) },
        trailingIcon = {
            if (isPassword) {
                val icon = if (showPassword.value) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }
                IconButton(onClick = { showPassword.value = !showPassword.value }) {
                    Icon(imageVector = icon, contentDescription = "Visibility")
                }
            }
        }
    )
}