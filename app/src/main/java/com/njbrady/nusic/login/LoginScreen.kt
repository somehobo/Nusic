package com.njbrady.nusic.login

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.login.composables.PasswordField
import com.njbrady.nusic.login.composables.UsernameField
import com.njbrady.nusic.login.model.LoginScreenViewModel
import com.njbrady.nusic.login.model.GeneralStates
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
                    loginScreenViewModel, navController
                )
            }
            composable(LoginScreens.Register.route) {
                RegisterScreen(
                    loginScreenViewModel, navController
                )
            }
        }
    }
}

@Composable
private fun LoginContent(
    loginScreenViewModel: LoginScreenViewModel, navController: NavController
) {
    val focusManager = LocalFocusManager.current
    val username by loginScreenViewModel.userNameInput.collectAsState()
    val password by loginScreenViewModel.passwordInput.collectAsState()
    val loginState by loginScreenViewModel.loginState.collectAsState()
    val usernameErrorMessage by loginScreenViewModel.userNameErrorMessages.collectAsState()
    val passwordErrorMessage by loginScreenViewModel.passwordErrorMessages.collectAsState()
    val errorMessage by loginScreenViewModel.errorMessage.collectAsState()
    Scaffold { paddingValues ->
        if (loginState == GeneralStates.Loading) {
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
                    .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.NusicDimenX2))
                )
                UsernameField(
                    modifier = loginFieldModifier,
                    hint = stringResource(id = R.string.username_field_hint),
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
                    hint = stringResource(id = R.string.password_field_hint),
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
                        Text(text = stringResource(id = R.string.login_button))
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.NusicDimenX1)),
                        text = stringResource(id = R.string.or_middle_text),
                        style = MaterialTheme.typography.body1
                    )
                    Button(onClick = {
                        navController.navigate(LoginScreens.Register.route)
                    }) {
                        Text(text = stringResource(id = R.string.create_account_button))
                    }
                }
            }
            if (errorMessage.isNotEmpty()) {
                errorMessage.forEach {
                    Toast.makeText(
                        LocalContext.current, it, Toast.LENGTH_LONG
                    ).show()
                }
                loginScreenViewModel.resetLoginState()
                loginScreenViewModel.resetGeneralErrorState()
            } else if (loginState == GeneralStates.Success) {
                Toast.makeText(
                    LocalContext.current,
                    stringResource(id = R.string.login_success_toast),
                    Toast.LENGTH_LONG
                ).show()
                loginScreenViewModel.resetLoginScreenState()
            }
        }
    }
}

sealed class LoginScreens(val route: String, @StringRes val resourceId: Int) {
    object Login : LoginScreens("Login", R.string.login_screen)
    object Register : LoginScreens("Register", R.string.register_screen)
}