package com.njbrady.nusic.login

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.LoginField
import com.njbrady.nusic.login.data.LoginScreenViewModel
import com.njbrady.nusic.login.data.LoginStates
import com.njbrady.nusic.login.data.RegisterScreen

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
    val username by loginScreenViewModel.userNameInput.collectAsState()
    val password by loginScreenViewModel.passwordInput.collectAsState()
    val loginState by loginScreenViewModel.loginState.collectAsState()
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
                    .fillMaxWidth(0.6f)
                    .padding(8.dp)
                Text(
                    text = "Nusic",
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
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
                        text = "Or",
                        style = MaterialTheme.typography.body1
                    )
                    Button(onClick = {
                        navController.navigate(LoginScreens.Register.route)
                    }) {
                        Text(text = "Create Account")
                    }
                }
            }
            if (loginState == LoginStates.Error) {
                Toast.makeText(
                    LocalContext.current,
                    loginScreenViewModel.errorMessage,
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetLoginScreenState()
            } else if (loginState == LoginStates.Success) {
                Toast.makeText(
                    LocalContext.current,
                    "Successfully logged in",
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetLoginState()
            }
        }
    }
}

sealed class LoginScreens(val route: String, @StringRes val resourceId: Int) {
    object Login : LoginScreens("login", R.string.login_screen)
    object Register : LoginScreens("Register", R.string.register_screen)
}