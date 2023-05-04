package com.njbrady.nusic.profile

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun OtherProfileScreen(profileViewModel: ProfileViewModel) {
    Scaffold() {paddingValues->
        Text(text = profileViewModel.userName)
    }
}