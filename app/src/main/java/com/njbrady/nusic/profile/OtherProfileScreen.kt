package com.njbrady.nusic.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.LocalNavController
import com.njbrady.nusic.ProfileScreenContent
import com.njbrady.nusic.ProfileScreens
import com.njbrady.nusic.Screen
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.upload.UploadScreen
import com.njbrady.nusic.upload.UploadScreenViewModel

@Composable
fun OtherProfileScreen(profileViewModel: ProfileViewModel) {
    val otherProfileNavController = rememberNavController()
    val currentlySelected by profileViewModel.currentlySelected.collectAsState()


    Scaffold { paddingValues ->
        NavHost(
            navController = otherProfileNavController,
            startDestination = ProfileScreens.ProfileHome.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ProfileScreens.ProfileHome.route) {
                profileViewModel.pauseAndReset()
                ProfileScreenContent(
                    profileViewModel = profileViewModel,
                    currentlySelected = currentlySelected,
                    onFilter = { newFilter -> profileViewModel.setCurrentlySelected(newFilter) },
                    onSelected = { index ->
                        profileViewModel.selectedSongIndex = index
                        otherProfileNavController.navigate(ProfileScreens.LCSongs.route)
                    },
                    onUploadHit = {},
                    visiting = true,
                )
            }
            composable(ProfileScreens.LCSongs.route) {
                ProfileScrollingSongs(
                    profileViewModel = profileViewModel,
                    songListType = currentlySelected,
                    navController = otherProfileNavController
                )
            }
        }
    }
}

@Composable
private fun ProfileScrenNavigation(
    profileViewModel: ProfileViewModel,
    uploadScreenViewModel: UploadScreenViewModel,
    profileNavController: NavHostController,
) {

}
