package com.njbrady.nusic.login.composables

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun LoginField(
    hint: String,
    value: String,
    isPassword: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val showPassword = remember { mutableStateOf(isPassword.not()) }
    var keyboardType = KeyboardType.Ascii
    if (isPassword) {
        keyboardType = KeyboardType.Password
    }
    val keyboardOptions = KeyboardOptions(keyboardType = keyboardType)

    TextField(
        modifier = modifier,
        value = value,
        placeholder = { Text(text = hint) },
        visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        onValueChange = { onValueChange(it) },
        trailingIcon = {
            if (isPassword) {
                val icon = if (showPassword.value) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }
                IconButton(onClick = { showPassword.value = !showPassword.value }) {
                    Icon(imageVector = icon, contentDescription = "Visibility")
                }
            }
        },
        keyboardOptions = keyboardOptions
    )
}
