package com.njbrady.nusic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues.calculateBottomPadding())
        ) {
            Row {
                Button(onClick = { mainViewModel.logout()
                    mainViewModel.getOnLogoutHit()()
                }) {
                    Text(text = "Logout")
                }
            }
        }
    }
}