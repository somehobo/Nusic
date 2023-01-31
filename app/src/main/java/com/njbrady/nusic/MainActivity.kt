package com.njbrady.nusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.home.HomeScreen
import com.njbrady.nusic.home.HomeScreenViewModel
import com.njbrady.nusic.login.LoginScreen
import com.njbrady.nusic.login.model.LoginScreenViewModel
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.TokenStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    private val loginScreenViewModel: LoginScreenViewModel by viewModels()
    private val homeScreenViewModel: HomeScreenViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginState = tokenStorage.containsToken.collectAsState()
            NusicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {

                    if (loginState.value) {
                        MainContent(homeScreenViewModel, mainViewModel)
                    } else {
                        LoginScreen(loginScreenViewModel = loginScreenViewModel)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        homeScreenViewModel.pauseCurrent()
    }

    override fun onResume() {
        super.onResume()
        homeScreenViewModel.resumeCurrent()
    }
}


@Composable
private fun MainContent(homeScreenViewModel: HomeScreenViewModel, mainViewModel: MainViewModel) {
    mainViewModel.setOnLogoutHit { homeScreenViewModel.resetState() }
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        BottomNavigation {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            screens.forEach { screen ->
                BottomNavigationItem(icon = {
                    Icon(
                        Icons.Filled.Favorite, contentDescription = null
                    )
                },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
            }
        }
    }) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(mainViewModel) }
            composable(Screen.Home.route) {
                homeScreenViewModel.retry()
                HomeScreen(homeScreenViewModel, navController)
            }
            composable(Screen.Conversation.route) { ConversationScreen() }
        }
    }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home_screen)
    object Conversation : Screen("conversation", R.string.conversation_screen)
    object Profile : Screen("profile", R.string.profile_screen)
}

val screens = listOf(
    Screen.Profile, Screen.Home, Screen.Conversation
)
