package com.njbrady.nusic.login

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.*
import com.njbrady.nusic.login.model.LoginScreenViewModel
import com.njbrady.nusic.login.model.LoginStates
import com.njbrady.nusic.login.model.RegisterScreen

@Composable
fun LoginScreen(loginScreenViewModel: LoginScreenViewModel) {
    LoginNavigation(loginScreenViewModel = loginScreenViewModel)
}

@Composable
private fun LoginNavigation(loginScreenViewModel: LoginScreenViewModel) {
    val navController = rememberNavController()
    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = LoginScreens.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(LoginScreens.Login.route) {
                LoginContent(
                    loginScreenViewModel,
                    navController
                )
            }
            composable(LoginScreens.Register.route) {
                RegisterScreen(
                    loginScreenViewModel,
                    navController
                )
            }
        }
    }
}

@Composable
private fun LoginContent(
    loginScreenViewModel: LoginScreenViewModel,
    navController: NavController
) {
    val focusManager = LocalFocusManager.current
    val username by loginScreenViewModel.userNameInput.collectAsState()
    val password by loginScreenViewModel.passwordInput.collectAsState()
    val loginState by loginScreenViewModel.loginState.collectAsState()
    val usernameErrorMessage by loginScreenViewModel.userNameErrorMessages.collectAsState()
    val passwordErrorMessage by loginScreenViewModel.passwordErrorMessages.collectAsState()
    val errorMessage by loginScreenViewModel.errorMessage.collectAsState()
    Scaffold { paddingValues ->
        if (loginState == LoginStates.Loading) {
            CenteredProgressIndicator(paddingValues = paddingValues)
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
                    .fillMaxWidth(0.8f)
                    .padding(8.dp)
                Text(
                    text = "Nusic",
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                UsernameField(
                    modifier = loginFieldModifier,
                    hint = "Username",
                    value = username,
                    onValueChange = { input -> loginScreenViewModel.setUserName(input) },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    imeAction = ImeAction.Next,
                )
                usernameErrorMessage.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                PasswordField(
                    modifier = loginFieldModifier,
                    hint = "Password",
                    value = password,
                    onValueChange = { input -> loginScreenViewModel.setPassword(input) },
                    onGo = { loginScreenViewModel.attemptLogin() },
                    imeAction = ImeAction.Go,
                )
                passwordErrorMessage.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { loginScreenViewModel.attemptLogin() }) {
                        Text(text = "Login")
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = "or",
                        style = MaterialTheme.typography.body1
                    )
                    Button(onClick = {
                        navController.navigate(LoginScreens.Register.route)
                    }) {
                        Text(text = "Create Account")
                    }
                }
            }
            if (errorMessage.isNotEmpty()) {
                errorMessage.forEach {
                    Toast.makeText(
                        LocalContext.current,
                        it,
                        Toast.LENGTH_LONG
                    ).show()
                }
                loginScreenViewModel.resetLoginState()
                loginScreenViewModel.resetGeneralErrorState()
            } else if (loginState == LoginStates.Success) {
                Toast.makeText(
                    LocalContext.current,
                    "Successfully logged in",
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetLoginScreenState()
            }
        }
    }
}

sealed class LoginScreens(val route: String, @StringRes val resourceId: Int) {
    object Login : LoginScreens("login", R.string.login_screen)
    object Register : LoginScreens("Register", R.string.register_screen)
}