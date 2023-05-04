package com.njbrady.nusic

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.njbrady.nusic.home.HomeScreen
import com.njbrady.nusic.home.HomeScreenViewModel
import com.njbrady.nusic.login.LoginActivity
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.profile.OtherProfileScreen
import com.njbrady.nusic.profile.ProfileViewModel
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.upload.UploadScreenViewModel
import com.njbrady.nusic.utils.UserModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Nolan is my baby and im so lucky to have been w him for 21 months <3


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenViewModel: HomeScreenViewModel by viewModels()
    private val uploadScreenViewModel: UploadScreenViewModel by viewModels()

    @Inject
    lateinit var otherProfileFactory: ProfileViewModel.Factory

    private val profileViewModel by viewModels<ProfileViewModel> {
        ProfileViewModel.provideProfileViewModelFactory(otherProfileFactory, null)
    }

    @Inject
    lateinit var mainSocketHandler: MainSocketHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.setOnLogoutHit {
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }
        setContent {
            NusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    MainContent(
                        homeScreenViewModel,
                        profileViewModel,
                        uploadScreenViewModel,
                        mainSocketHandler,
                        otherProfileFactory
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        profileViewModel.pauseCurrent()
        homeScreenViewModel.tempPauseCurrent()
        uploadScreenViewModel.pauseWhenReady()
    }

}


@Composable
private fun MainContent(
    homeScreenViewModel: HomeScreenViewModel,
    profileViewModel: ProfileViewModel,
    uploadScreenViewModel: UploadScreenViewModel,
    mainSocketHandler: MainSocketHandler,
    otherProfileViewModelFactory: ProfileViewModel.Factory
) {
    val navController = rememberNavController()
    val socketState by mainSocketHandler.connected.collectAsState()
    val loadingConnection by mainSocketHandler.loadingConnection.collectAsState()
    val socketError by mainSocketHandler.errorMessage.collectAsState()

    Scaffold(bottomBar = {
        Column {
            BottomNavigation(backgroundColor = MaterialTheme.colors.background) {
                socketError?.let {
                    Toast.makeText(LocalContext.current, socketError, Toast.LENGTH_SHORT).show()
                    mainSocketHandler.resetErrorMessage()
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                BottomNavigationItem(icon = {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = null
                    )
                },
                    label = { Text(stringResource(Screen.Home.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                BottomNavigationItem(icon = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                    label = { Text(stringResource(Screen.Profile.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Profile.route } == true,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    })
            }
        }

        if (!socketState) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.error)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (loadingConnection) {
                    CenteredProgressIndicator()
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.lost_socket_connection),
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onError,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(
                                    id = R.dimen.NusicDimenX2
                                ), vertical = dimensionResource(id = R.dimen.NusicDimenX1)
                            )
                        )
                        Button(
                            onClick = { mainSocketHandler.retryConnection() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.background,
                                contentColor = MaterialTheme.colors.onBackground,
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(id = R.string.refresh_button_content_description),
                            )
                        }
                    }
                }
            }
        }
    }
    ) { innerPadding ->
        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(
                navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)
            ) {

                composable(Screen.Home.route) {
                    profileViewModel.pauseCurrent()
                    uploadScreenViewModel.pauseWhenReady()
                    HomeScreen(homeScreenViewModel, navController)
                }

                composable(
                    Screen.OtherProfile.route,
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    backStackEntry.arguments?.let {
                        val userId = it.getInt("userId")
                        val userName = it.getString("userName", "No Username Provided")
                        val userModel = UserModel(userName, userId)
                        val otherProfileViewModel = viewModel<ProfileViewModel>(
                            factory = ProfileViewModel.provideProfileViewModelFactory(
                                otherProfileViewModelFactory,
                                userModel
                            )
                        )

                        OtherProfileScreen(profileViewModel = otherProfileViewModel)

                    }
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        mainViewModel = profileViewModel,
                        uploadScreenViewModel = uploadScreenViewModel
                    )
                }
            }
        }
    }
}

val LocalNavController = compositionLocalOf<NavController> {
    error("No NavController provided")
}

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("HomePage", R.string.home_screen)
    object Profile : Screen("ProfilePage", R.string.profile_screen)

    object OtherProfile :
        Screen("OtherProfilePage/{userId}/{userName}", R.string.other_profile_page) {
        fun createRoute(userId: Int, userName: String) = "OtherProfilePage/$userId/$userName"
    }

}

//sealed class OtherProfileScreens(val route: String, @StringRes val resourceId: Int) {
//    object OtherProfileHome : ProfileScreens("OtherProfileHome", R.string.other_profile_home)
//    object OtherLCSongs : ProfileScreens("OtherLCSongs", R.string.other_scrolling_songs_screen)
//
//}

sealed class ProfileScreens(val route: String, @StringRes val resourceId: Int) {
    object ProfileHome : ProfileScreens("ProfileHome", R.string.profile_home)
    object LCSongs : ProfileScreens("LCSongs", R.string.scrolling_songs_screen)
    object Upload : ProfileScreens("Upload", R.string.upload_song)
}
