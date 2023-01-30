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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.*

@Composable
fun RegisterScreen(loginScreenViewModel: LoginScreenViewModel, navController: NavController) {
    RegisterContent(loginScreenViewModel, navController)
}

@Composable
private fun RegisterContent(
    loginScreenViewModel: LoginScreenViewModel, navController: NavController
) {
    val focusManager = LocalFocusManager.current
    val registerUserName by loginScreenViewModel.registerUserNameInput.collectAsState()
    val registerPassword by loginScreenViewModel.registerPasswordInput.collectAsState()
    val registerSecondaryPassword by loginScreenViewModel.registerSecondaryPasswordInput.collectAsState()
    val registerEmail by loginScreenViewModel.registerEmailInput.collectAsState()
    val registerState by loginScreenViewModel.loginState.collectAsState()
    val loginFieldModifier = Modifier
        .fillMaxWidth(0.8f)
        .padding(dimensionResource(id = R.dimen.NusicDimenX1))
    val registerUserNameErrors by loginScreenViewModel.registerUserNameErrorMessages.collectAsState()
    val registerEmailErrors by loginScreenViewModel.registerEmailErrorMessages.collectAsState()
    val registerPasswordErrors by loginScreenViewModel.registerPasswordErrorMessages.collectAsState()
    val errorMessage by loginScreenViewModel.errorMessage.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = stringResource(id = R.string.register_screen),
                style = MaterialTheme.typography.h5
            )
        }, navigationIcon = if (navController.previousBackStackEntry != null) {
            {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button_content_description)
                    )
                }
            }
        } else {
            null
        }

        )
    }) { paddingValues ->
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
                UsernameField(
                    modifier = loginFieldModifier,
                    hint = stringResource(id = R.string.username_field_hint),
                    value = registerUserName,
                    onValueChange = { input -> loginScreenViewModel.setRegisterUserName(input) },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    imeAction = ImeAction.Next,
                )
                registerUserNameErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                EmailField(
                    modifier = loginFieldModifier,
                    hint = stringResource(id = R.string.email_field_hint),
                    value = registerEmail,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onValueChange = { input -> loginScreenViewModel.setRegisterEmailInput(input) },
                    imeAction = ImeAction.Next
                )
                registerEmailErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                PasswordField(
                    modifier = loginFieldModifier,
                    hint = stringResource(id = R.string.password_field_hint),
                    value = registerPassword,
                    onValueChange = { input -> loginScreenViewModel.setRegisterPassword(input) },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    imeAction = ImeAction.Next,
                )
                registerPasswordErrors.forEach {
                    ErrorWithField(message = it, modifier = loginFieldModifier)
                }
                PasswordField(
                    modifier = loginFieldModifier,
                    hint = stringResource(id = R.string.confirm_password_field_hint),
                    value = registerSecondaryPassword,
                    onValueChange = { input ->
                        loginScreenViewModel.setRegisterSecondaryPassword(
                            input
                        )
                    },
                    onGo = { loginScreenViewModel.attemptRegisterUser() },
                    imeAction = ImeAction.Go,
                )
                Button(onClick = { loginScreenViewModel.attemptRegisterUser() }) {
                    Text(text = stringResource(id = R.string.register_screen))
                }

                if (errorMessage.isNotEmpty()) {
                    errorMessage.forEach {
                        Toast.makeText(
                            LocalContext.current, it, Toast.LENGTH_LONG
                        ).show()
                    }
                    loginScreenViewModel.resetLoginState()
                    loginScreenViewModel.resetGeneralErrorState()
                } else if (registerState == LoginStates.Success) {
                    Toast.makeText(
                        LocalContext.current,
                        stringResource(id = R.string.register_success_toast),
                        Toast.LENGTH_LONG
                    ).show()
                    loginScreenViewModel.resetRegisterScreenState()
                }
            }
        }
    }
}