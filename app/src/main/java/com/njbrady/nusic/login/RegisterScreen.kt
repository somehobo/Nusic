package com.njbrady.nusic.login.model

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
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
    val registerUserName by loginScreenViewModel.registerUserNameInput.collectAsState()
    val registerPassword by loginScreenViewModel.registerPasswordInput.collectAsState()
    val registerSecondaryPassword by
    loginScreenViewModel.registerSecondaryPasswordInput.collectAsState()
    val registerEmail by loginScreenViewModel.registerEmailInput.collectAsState()
    val registerState by loginScreenViewModel.loginState.collectAsState()
    val loginFieldModifier = Modifier
        .fillMaxWidth(0.6f)
        .padding(8.dp)
    val registerUserNameErrors by loginScreenViewModel.registerUserNameErrorMessages.collectAsState()
    val registerEmailErrors by loginScreenViewModel.registerEmailErrorMessages.collectAsState()
    val registerPasswordErrors by loginScreenViewModel.registerPasswordErrorMessages.collectAsState()
    val errorMessage by loginScreenViewModel.errorMessage.collectAsState()
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
        if (registerState == LoginStates.Loading) {
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
                    value = registerUserName,
                    isPassword = false,
                    onValueChange = { input -> loginScreenViewModel.setRegisterUserName(input) },
                    modifier = loginFieldModifier
                )
                registerUserNameErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                LoginField(
                    hint = "Email",
                    value = registerEmail,
                    isPassword = false,
                    onValueChange = { input -> loginScreenViewModel.setRegisterEmailInput(input) },
                    modifier = loginFieldModifier
                )
                registerEmailErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                LoginField(
                    hint = "Password",
                    value = registerPassword,
                    isPassword = true,
                    onValueChange = { input -> loginScreenViewModel.setRegisterPassword(input) },
                    modifier = loginFieldModifier
                )
                registerPasswordErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                LoginField(
                    hint = "Confirm Password",
                    value = registerSecondaryPassword,
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
                } else if (registerState == LoginStates.Success) {
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