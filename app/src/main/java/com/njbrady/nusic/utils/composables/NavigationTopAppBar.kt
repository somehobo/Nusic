package com.njbrady.nusic.utils.composables

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.njbrady.nusic.R

@Composable
fun NavigationTopAppBar(navController: NavController, title: String, onBackClick: () -> Unit = {}) {
    return TopAppBar(title = {
        Text(
            text = title,
            style = MaterialTheme.typography.h5
        )
    }, navigationIcon = if (navController.previousBackStackEntry != null) {
        {
            IconButton(onClick = { navController.navigateUp()
                onBackClick()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back_button_content_description)
                )
            }
        }
    } else {
        null
    })
}