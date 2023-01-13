package com.njbrady.nusic.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.ConversationScreen
import com.njbrady.nusic.HomeScreen
import com.njbrady.nusic.ProfileScreen
import com.njbrady.nusic.R
import com.njbrady.nusic.login.LoginScreen
import com.njbrady.nusic.login.data.LoginScreenViewModel
import com.njbrady.nusic.ui.theme.NusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel = hiltViewModel<MainViewModel>()
            val loginState = mainViewModel.tokenStorage.containsToken.collectAsState()
            val loginScreenViewModel = hiltViewModel<LoginScreenViewModel>()
            NusicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {

                    if (loginState.value) {
                        MainContent(mainViewModel)
                    } else {
                        LoginScreen(loginScreenViewModel = loginScreenViewModel)
                    }
                }
            }
        }
    }
}


@Composable
private fun MainContent(mainViewModel: MainViewModel) {
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
            composable(Screen.Home.route) { HomeScreen(mainViewModel) }
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
