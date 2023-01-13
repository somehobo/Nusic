package com.njbrady.nusic.login.data

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.LoginField

@Composable
fun RegisterScreen(loginScreenViewModel: LoginScreenViewModel, navController: NavController) {
    RegisterContent(loginScreenViewModel, navController)
}

@Composable
private fun RegisterContent(
    loginScreenViewModel: LoginScreenViewModel,
    navController: NavController
) {
    val registerUserName = loginScreenViewModel.registerUserNameInput.collectAsState()
    val registerPassword = loginScreenViewModel.registerPasswordInput.collectAsState()
    val registerSecondaryPassword =
        loginScreenViewModel.registerSecondaryPasswordInput.collectAsState()
    val registerEmail = loginScreenViewModel.registerEmailInput.collectAsState()
    val registerState = loginScreenViewModel.loginState.collectAsState()
    val loginFieldModifier = Modifier
        .fillMaxWidth(0.6f)
        .padding(8.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Register", style = MaterialTheme.typography.h5)
                },
                navigationIcon = if (navController.previousBackStackEntry != null) {
                    {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else {
                    null
                }

            )
        }
    ) { paddingValues ->
        if (registerState.value == LoginStates.Loading) {
            CenteredProgressIndicator(paddingValues = paddingValues)
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginField(
                    hint = "Username",
                    value = registerUserName.value,
                    isPassword = false,
                    onValueChange = { input -> loginScreenViewModel.setRegisterUserName(input) },
                    modifier = loginFieldModifier
                )
                LoginField(
                    hint = "Email",
                    value = registerEmail.value,
                    isPassword = false,
                    onValueChange = { input -> loginScreenViewModel.setRegisterEmailInput(input) },
                    modifier = loginFieldModifier
                )
                LoginField(
                    hint = "Password",
                    value = registerPassword.value,
                    isPassword = true,
                    onValueChange = { input -> loginScreenViewModel.setRegisterPassword(input) },
                    modifier = loginFieldModifier

                )
                LoginField(
                    hint = "Confirm Password",
                    value = registerSecondaryPassword.value,
                    isPassword = true,
                    onValueChange = { input ->
                        loginScreenViewModel.setRegisterSecondaryPassword(
                            input
                        )
                    },
                    modifier = loginFieldModifier
                )
                Button(onClick = { loginScreenViewModel.attemptRegisterUser() }) {
                    Text(text = "Register")
                }

                if (registerState.value == LoginStates.Error) {
                    Toast.makeText(
                        LocalContext.current,
                        loginScreenViewModel.errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    loginScreenViewModel.resetLoginState()
                } else if (registerState.value == LoginStates.Success) {
                    Toast.makeText(
                        LocalContext.current,
                        "Successfully registered",
                        Toast.LENGTH_LONG
                    ).show()
                    loginScreenViewModel.resetRegisterScreenState()
                }
            }
        }
    }
}