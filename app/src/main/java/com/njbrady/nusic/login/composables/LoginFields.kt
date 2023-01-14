package com.njbrady.nusic.login.composables

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*


val whiteSpaceRegex = Regex("\\s+")

@Composable
fun UsernameField(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onGo: () -> Unit = {},
    imeAction: ImeAction,
) {
    TextField(
        modifier = modifier,
        value = value,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = imeAction,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onPrevious = { onPrevious() },
            onGo = { onGo() }),
        onValueChange = { input ->
            if (!input.contains(whiteSpaceRegex)) {
                onValueChange(input)
            }
        },
        placeholder = { Text(text = hint) },
    )
}

@Composable
fun PasswordField(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onGo: () -> Unit = {},
    imeAction: ImeAction,
) {
    val showPassword = remember { mutableStateOf(false) }

    TextField(
        modifier = modifier,
        value = value,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = imeAction,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onPrevious = { onPrevious() },
            onGo = { onGo() }),
        onValueChange = { input ->
            onValueChange(input)
        },
        visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        placeholder = { Text(text = hint) },
        trailingIcon = {
            val icon = if (showPassword.value) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            }
            IconButton(onClick = { showPassword.value = !showPassword.value }) {
                Icon(imageVector = icon, contentDescription = "Visibility")
            }
        },
    )
}

@Composable
fun EmailField(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onGo: () -> Unit = {},
    imeAction: ImeAction,
) {
    TextField(
        modifier = modifier,
        value = value,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = imeAction,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
            onPrevious = { onPrevious() },
            onGo = { onGo() }),
        onValueChange = { input ->
            if (!input.contains(whiteSpaceRegex)) {
                onValueChange(input)
            }
        },
        placeholder = { Text(text = hint) },
    )
}

